package com.coing.domain.notification.service

import com.coing.domain.notification.entity.OneMinuteRate
import com.coing.domain.notification.entity.TradeImpact
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class SubscribeManager(
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend fun updateTopicsAsync(
        tokens: List<String>,
        marketCode: String,
        newRate: OneMinuteRate,
        oldRate: OneMinuteRate,
        newImpact: TradeImpact,
        oldImpact: TradeImpact
    ) = withContext(Dispatchers.IO) {
        try {
            if (oldRate != OneMinuteRate.NONE) {
                firebaseMessaging.unsubscribeFromTopic(tokens, "$marketCode-HIGH-${oldRate.name}")
                firebaseMessaging.unsubscribeFromTopic(tokens, "$marketCode-LOW-${oldRate.name}")
            }

            if (newRate != OneMinuteRate.NONE) {
                firebaseMessaging.subscribeToTopic(tokens, "$marketCode-HIGH-${newRate.name}")
                firebaseMessaging.subscribeToTopic(tokens, "$marketCode-LOW-${newRate.name}")
            }

            if (oldImpact != TradeImpact.NONE) {
                firebaseMessaging.unsubscribeFromTopic(tokens, "$marketCode-HIGH-${oldImpact.name}")
                firebaseMessaging.unsubscribeFromTopic(tokens, "$marketCode-LOW-${oldImpact.name}")
            }

            if (newImpact != TradeImpact.NONE) {
                firebaseMessaging.subscribeToTopic(tokens, "$marketCode-HIGH-${newImpact.name}")
                firebaseMessaging.subscribeToTopic(tokens, "$marketCode-LOW-${newImpact.name}")
            }
        } catch (e: Exception) {
            // 실패해도 예외는 삼켜서 main flow에 영향 X
            println("FCM 구독 변경 실패: ${e.message}")
        }
    }
}
