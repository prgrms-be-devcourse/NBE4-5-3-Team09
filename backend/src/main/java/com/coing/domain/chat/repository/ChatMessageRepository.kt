package com.coing.domain.chat.repository

import com.coing.domain.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findAllByChatRoomIdAndTimestampAfter(chatRoomId: Long, timestamp: LocalDateTime): List<ChatMessage>
}
