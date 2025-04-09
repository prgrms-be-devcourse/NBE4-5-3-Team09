package com.coing.domain.chat.service

import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.entity.ChatMessageReport
import com.coing.domain.chat.dto.ChatMessageReportDto
import com.coing.domain.chat.repository.ChatMessageReportRepository
import com.coing.domain.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatReportService(
    private val chatMessageReportRepository: ChatMessageReportRepository
) {
    @Transactional
    fun reportMessage(chatMessage: ChatMessage, reporter: User): ChatMessageReport {
        chatMessage.id?.let {
            // 동일 메시지에 대해 같은 사용자가 이미 신고했는지 확인
            val existingReport = chatMessageReportRepository.findByChatMessageIdAndReporterId(it, reporter.id!!)
            if (existingReport != null) {
                throw IllegalArgumentException("이미 신고한 메시지입니다.")
            }
        }
        val report = ChatMessageReport(
            chatMessage = chatMessage,
            reporter = reporter
        )
        return chatMessageReportRepository.save(report)
    }

    fun getReportedMessages(threshold: Long = 3): List<ChatMessageReportDto> {
        return chatMessageReportRepository.findReportedMessages(threshold)
    }
}
