package com.coing.domain.chat.service

import com.coing.domain.chat.dto.ChatMessageReportDto
import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.entity.ChatMessageReport
import com.coing.domain.chat.repository.ChatMessageReportRepository
import com.coing.domain.chat.repository.ChatMessageRepository
import com.coing.domain.user.entity.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatReportService(
    private val chatMessageReportRepository: ChatMessageReportRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatService: ChatService,
    private val chatHelperService: ChatHelperService
) {
    private val log = LoggerFactory.getLogger(ChatReportService::class.java)

    // 신고한 사용자 집합: 메시지 ID -> Set of reporter UUIDs
    private val reportUniqueMap = ConcurrentHashMap<String, MutableSet<UUID>>()

    /**
     * 신고 로직
     * 1) 동일 메시지에 대해, 같은 사용자가 이미 신고한 경우 예외 처리
     * 2) 고유 신고자 집합에 신고자 추가 후, 집합의 크기가 3 미만이면 DB에 저장하지 않고 접수만 처리
     * 3) 고유 신고자 수가 3 이상이면, persistReportedMessage()로 영속화한 후,
     *    영속 상태의 메시지를 다시 조회하고, 신고 내역을 DB에 저장한 뒤 해당 메시지에 대해 집계된 캐시를 제거합니다.
     */
    @Transactional
    fun reportMessage(chatMessage: ChatMessage, reporter: User): ChatMessageReport? {
        // 신고자가 본인이 작성한 메시지를 신고할 수 없도록 예외 처리
        if (chatMessage.sender?.id == reporter.id) {
            throw IllegalArgumentException("본인이 작성한 메시지는 신고할 수 없습니다.")
        }

        // 메시지 ID는 null이 아니어야 함
        val messageId = chatMessage.id ?: throw IllegalStateException("메시지 ID가 없습니다.")

        // 해당 메시지에 대해 신고한 사용자 집합을 가져오거나 새로 생성
        val reporterSet = reportUniqueMap.computeIfAbsent(messageId) { ConcurrentHashMap.newKeySet() }
        // 같은 사용자가 이미 신고한 경우 처리
        if (!reporterSet.add(reporter.id!!)) {
            throw IllegalArgumentException("이미 신고한 메시지입니다.")
        }

        val count = reporterSet.size
        log.info("Message {} 신고 횟수 (고유 신고자 수): {}", messageId, count)

        // 신고 횟수가 3 미만이면 아직 DB에 저장하지 않고 접수만 처리
        if (count < 3) {
            return null
        }

        // 신고 횟수가 3 이상인 경우, 메시지가 DB에 영속되지 않은 경우 persistReportedMessage() 호출 후 재조회
        var persistentMessage = chatMessage
        if (!isPersisted(chatMessage)) {
            persistentMessage = chatService.persistReportedMessage(chatMessage)
            persistentMessage = chatMessageRepository.findById(persistentMessage.id!!).orElseThrow {
                IllegalStateException("메시지가 DB에서 조회되지 않습니다.")
            }
        }

        // 신고 내역 저장 (신고 내역은 한 건으로 저장)
        val newReport = ChatMessageReport(
            chatMessage = persistentMessage,
            reporter = reporter,
            reportedAt = LocalDateTime.now()
        )
        val savedReport = chatMessageReportRepository.save(newReport)
        log.info("신고 기록 저장됨. Message {} 신고 횟수: {}", messageId, count)

        // DB 저장 후, 해당 메시지에 대한 고유 신고자 집합 제거
        reportUniqueMap.remove(messageId)
        return savedReport
    }

    /**
     * chatMessage가 DB에 저장되어 있으면 true, 아니면 false
     */
    private fun isPersisted(chatMessage: ChatMessage): Boolean {
        val msgId = chatMessage.id ?: return false
        return chatMessageRepository.existsById(msgId)
    }

    /**
     * 신고된 메시지 테이블의 모든 기록을 조회하여 전용 DTO로 변환한 후 반환합니다.
     */
    @Transactional(readOnly = true)
    fun getReportedMessages(): List<ChatMessageReportDto> {
        // 모든 신고 기록 조회
        val allReports: List<ChatMessageReport> = chatMessageReportRepository.findAll()

        // Lazy 로딩된 필드 초기화
        allReports.forEach { report ->
            report.chatMessage.apply {
                this.id
                this.content
                this.timestamp
                this.sender?.apply {
                    this.id
                    this.name
                    this.email
                }
            }
        }

        // 각 신고 기록을 전용 DTO로 변환
        return allReports.map { report ->
            ChatMessageReportDto(
                chatMessage = chatHelperService.convertToDto(report.chatMessage),
                reportCount = 1L // 신고 기록은 단일 행으로 저장
            )
        }
    }
}
