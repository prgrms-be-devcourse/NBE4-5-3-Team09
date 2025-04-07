package com.coing.domain.chat.repository

import com.coing.domain.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByChatRoomIdOrderByTimestampAsc(chatRoomId: Long): List<ChatMessage>
}
