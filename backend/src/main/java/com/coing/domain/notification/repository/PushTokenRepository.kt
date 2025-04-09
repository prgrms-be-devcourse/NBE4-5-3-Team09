package com.coing.domain.notification.repository

import com.coing.domain.notification.entity.PushToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*


interface PushTokenRepository : JpaRepository<PushToken, UUID> {
    fun findAllByUserId(userId: UUID): List<PushToken>
}
