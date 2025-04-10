package com.coing.domain.notification.service

import com.coing.domain.coin.market.repository.MarketRepository
import com.coing.domain.notification.dto.SubscribeInfo
import com.coing.domain.notification.entity.OneMinuteRate
import com.coing.domain.notification.entity.PushToken
import com.coing.domain.notification.entity.Subscribe
import com.coing.domain.notification.repository.PushTokenRepository
import com.coing.domain.notification.repository.SubscribeRepository
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PushService(
    private val userRepository: UserRepository,
    private val marketRepository: MarketRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val subscribeRepository: SubscribeRepository,
    private val messageUtil: MessageUtil,
    private val firebaseMessaging: FirebaseMessaging
) {
    private val log = LoggerFactory.getLogger(PushService::class.java)

    @Transactional
    fun saveToken(userId: UUID, token: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.NOT_FOUND) }

        val alreadyExists = pushTokenRepository.findAllByUserId(user.id!!).any { it.token == token }
        if (!alreadyExists) {
            pushTokenRepository.save(PushToken(user = user, token = token))
        }
    }

    @Transactional
    fun updateSubscription(userId: UUID, marketCode: String, newRate: OneMinuteRate, oldRate: OneMinuteRate) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.NOT_FOUND) }
        val market = marketRepository.findByCode(marketCode)
            ?: throw BusinessException("market.not.found", HttpStatus.NOT_FOUND)

        val tokens = pushTokenRepository.findAllByUserId(user.id!!).map { it.token }


        // topic 구독 해지
        if (oldRate != OneMinuteRate.NONE) {
            val level = oldRate.toLevel()
            firebaseMessaging.unsubscribeFromTopic(tokens, "$marketCode-HIGH-$level")
            firebaseMessaging.unsubscribeFromTopic(tokens, "$marketCode-LOW-$level")
        }

        // topic 구독 신청
        if (newRate != OneMinuteRate.NONE) {
            val level = newRate.toLevel()
            firebaseMessaging.subscribeToTopic(tokens, "$marketCode-HIGH-$level")
            firebaseMessaging.subscribeToTopic(tokens, "$marketCode-LOW-$level")
        }

        // 구독 테이블 업데이트
        val subscribe = subscribeRepository.findByUser_IdAndMarket_Code(user.id, market.code)
            ?: Subscribe(user = user, market = market)
        subscribe.oneMinuteRate = newRate

        subscribeRepository.save(subscribe)
    }

    @Transactional(readOnly = true)
    fun getSubscribeInfo(userId: UUID, marketCode: String): SubscribeInfo {
        val subscribe = subscribeRepository.findByUser_IdAndMarket_Code(userId, marketCode)
        return SubscribeInfo(subscribe?.oneMinuteRate ?: OneMinuteRate.NONE)
    }

    suspend fun sendAsync(title: String, body: String, marketCode: String, topic: String) =
        withContext(Dispatchers.IO) {
            try {
                val message = Message.builder()
                    .setTopic(topic)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("url", "/coin/$marketCode")
                    .build()

                log.info("topic: {}", topic)

                firebaseMessaging.send(message)
            } catch (e: Exception) {
                log.error("FCM 전송 실패: ", e)
            }
        }
}
