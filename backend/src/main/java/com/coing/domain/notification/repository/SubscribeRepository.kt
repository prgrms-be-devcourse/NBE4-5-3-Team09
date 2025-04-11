package com.coing.domain.notification.repository

import com.coing.domain.notification.entity.Subscribe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SubscribeRepository : JpaRepository<Subscribe, Long> {
    fun findByUser_IdAndMarket_Code(userId: UUID, marketCode: String): Subscribe?
}
