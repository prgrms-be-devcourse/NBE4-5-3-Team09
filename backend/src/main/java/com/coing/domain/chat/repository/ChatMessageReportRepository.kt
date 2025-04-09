package com.coing.domain.chat.repository

import com.coing.domain.chat.dto.ChatMessageReportDto
import com.coing.domain.chat.entity.ChatMessageReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatMessageReportRepository : JpaRepository<ChatMessageReport, Long> {

    fun findByChatMessageIdAndReporterId(chatMessageId: String, reporterId: java.util.UUID): ChatMessageReport?

    fun countByChatMessageId(chatMessageId: String): Long
}