package com.coing.domain.chat.dto

import com.coing.domain.chat.entity.ChatMessage

data class ChatMessageReportDto(
    val chatMessage: ChatMessage,
    val reportCount: Long
)
