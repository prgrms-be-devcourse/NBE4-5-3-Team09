package com.coing.domain.chat.dto

data class ChatMessageDto(
    var sender: String = "",
    var content: String = "",
    var timestamp: String = ""
)
