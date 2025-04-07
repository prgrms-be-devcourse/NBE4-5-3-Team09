package com.coing.domain.chat.repository

import com.coing.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    @Query("select cr from ChatRoom cr join cr.market m where m.code = ?1")
    fun findByMarketCode(marketCode: String): Optional<ChatRoom>
}
