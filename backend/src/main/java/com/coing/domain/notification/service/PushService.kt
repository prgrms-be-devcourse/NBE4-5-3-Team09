package com.coing.domain.notification.service

import com.coing.domain.coin.market.repository.MarketRepository
import com.coing.domain.notification.dto.SubscribeInfo
import com.coing.domain.notification.entity.OneMinuteRate
import com.coing.domain.notification.entity.PushToken
import com.coing.domain.notification.entity.Subscribe
import com.coing.domain.notification.entity.TradeImpact
import com.coing.domain.notification.repository.PushTokenRepository
import com.coing.domain.notification.repository.SubscribeRepository
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val firebaseMessaging: FirebaseMessaging,
    private val subscribeManager: SubscribeManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
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
    fun updateSubscription(
        userId: UUID,
        marketCode: String,
        newRate: OneMinuteRate,
        oldRate: OneMinuteRate,
        newImpact: TradeImpact,
        oldImpact: TradeImpact
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.NOT_FOUND) }
        val market = marketRepository.findByCode(marketCode)
            ?: throw BusinessException("market.not.found", HttpStatus.NOT_FOUND)

        val tokens = pushTokenRepository.findAllByUserId(user.id!!).map { it.token }

        // 코루틴으로 외부 작업 넘기기
        coroutineScope.launch {
            subscribeManager.updateTopicsAsync(tokens, marketCode, newRate, oldRate, newImpact, oldImpact)
        }

        // 구독 정보는 즉시 업데이트
        val subscribe = subscribeRepository.findByUser_IdAndMarket_Code(user.id, market.code)
            ?: Subscribe(user = user, market = market)

        subscribe.oneMinuteRate = newRate
        subscribe.tradeImpact = newImpact
        subscribeRepository.save(subscribe)
    }

    @Transactional(readOnly = true)
    fun getSubscribeInfo(userId: UUID, marketCode: String): SubscribeInfo {
        val subscribe = subscribeRepository.findByUser_IdAndMarket_Code(userId, marketCode)
        return SubscribeInfo(
            oneMinuteRate = subscribe?.oneMinuteRate ?: OneMinuteRate.NONE,
            tradeImpact = subscribe?.tradeImpact ?: TradeImpact.NONE
        )
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
