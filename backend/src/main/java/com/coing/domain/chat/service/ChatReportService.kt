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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class ChatReportService(
    private val chatMessageReportRepository: ChatMessageReportRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatService: ChatService,
    private val chatHelperService: ChatHelperService
) {
    private val log = LoggerFactory.getLogger(ChatReportService::class.java)

    // In-memory 신고 카운터 (메시지 ID -> 신고 횟수)
    private val reportCounts = ConcurrentHashMap<String, AtomicInteger>()

    /**
     * 신고 로직
     * 1) 중복 신고 여부 확인
     * 2) in-memory 신고 횟수를 증가
     * 3) 누적 신고 횟수가 3 미만이면 DB에는 신고 기록을 저장하지 않고 접수된 것으로 처리
     * 4) 신고 횟수가 3 이상이면, 메시지가 DB에 영속되지 않았다면 persistReportedMessage()로 영속화한 후,
     *    영속 상태의 객체를 재조회하여 사용하고, 이후 신고 레코드를 DB에 저장한 뒤 in-memory 카운터를 제거합니다.
     */
    @Transactional
    fun reportMessage(chatMessage: ChatMessage, reporter: User): ChatMessageReport? {
        // 신고자가 본인이 작성한 메시지인 경우 거부
        if (chatMessage.sender?.id == reporter.id) {
            throw IllegalArgumentException("본인이 작성한 메시지는 신고할 수 없습니다.")
        }

        // (1) 중복 신고 체크: DB에 이미 해당 (chatMessage, reporter) 신고 기록이 있다면 거부
        val existingReport = chatMessageReportRepository.findByChatMessageIdAndReporterId(
            chatMessage.id!!, reporter.id!!
        )
        if (existingReport != null) {
            throw IllegalArgumentException("이미 신고한 메시지입니다.")
        }

        // (2) in-memory 신고 횟수 증가
        val count = reportCounts.compute(chatMessage.id!!) { _, counter ->
            counter?.apply { incrementAndGet() } ?: AtomicInteger(1)
        }!!.get()
        log.info("Message {} 신고 횟수: {}", chatMessage.id, count)

        // (3) 신고 횟수가 3 미만이면, 아직 DB에 신고 기록을 남기지 않고 접수만 처리
        if (count < 3) {
            return null
        }

        // (4) 신고 횟수가 3 이상이면, 메시지가 DB에 영속되지 않았다면 persistReportedMessage() 호출 후 재조회
        var persistentMessage = chatMessage
        if (!isPersisted(chatMessage)) {
            persistentMessage = chatService.persistReportedMessage(chatMessage)
            persistentMessage = chatMessageRepository.findById(persistentMessage.id!!).orElseThrow {
                IllegalStateException("메시지가 DB에서 조회되지 않습니다.")
            }
        }

        // (5) 최종 신고 레코드 저장 (영속 상태의 메시지를 참조)
        val newReport = ChatMessageReport(
            chatMessage = persistentMessage,
            reporter = reporter,
            reportedAt = LocalDateTime.now()
        )
        val savedReport = chatMessageReportRepository.save(newReport)
        log.info("신고 기록 저장됨. Message {} 신고 횟수: {}", persistentMessage.id, count)

        // 신고 기준 충족 후 in-memory 카운터 제거
        reportCounts.remove(chatMessage.id)

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

        // Lazy 로딩된 필드를 강제 초기화
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

        // 각 신고 기록을 전용 DTO로 변환합니다.
        return allReports.map { report ->
            ChatMessageReportDto(
                chatMessage = chatHelperService.convertToDto(report.chatMessage),
                reportCount = 1L  // 각 신고 행은 단일 신고이므로 기본 값 1
            )
        }
    }
}
