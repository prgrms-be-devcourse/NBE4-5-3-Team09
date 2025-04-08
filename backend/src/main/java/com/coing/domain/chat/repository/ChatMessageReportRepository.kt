package com.coing.domain.chat.repository

import com.coing.domain.chat.entity.ChatMessageReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatMessageReportRepository : JpaRepository<ChatMessageReport, Long> {

    fun findByChatMessageIdAndReporterId(chatMessageId: Long, reporterId: java.util.UUID): ChatMessageReport?

    // 신고된 메시지를 그룹화하여, 신고 횟수가 threshold 이상인 메시지를 DTO로 반환
    @Query(
        "SELECT new com.coing.domain.chat.dto.ChatMessageReportDto(r.chatMessage, COUNT(r)) " +
                "FROM ChatMessageReport r GROUP BY r.chatMessage HAVING COUNT(r) >= :threshold"
    )
    fun findReportedMessages(@Param("threshold") threshold: Long): List<com.coing.domain.chat.dto.ChatMessageReportDto>
}
