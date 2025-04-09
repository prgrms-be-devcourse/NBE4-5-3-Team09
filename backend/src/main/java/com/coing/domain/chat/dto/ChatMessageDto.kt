package com.coing.domain.chat.dto

data class ChatMessageDto(
    var id: String? = null,
    var sender: String = "",
    var content: String = "",
    var timestamp: String = ""
)
