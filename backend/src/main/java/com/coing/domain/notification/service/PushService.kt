package com.coing.domain.notification.service

import com.coing.domain.bookmark.repository.BookmarkRepository
import com.coing.domain.notification.entity.PushToken
import com.coing.domain.notification.repository.PushTokenRepository
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PushService(
    private val userRepository: UserRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val messageUtil: MessageUtil,
    private val firebaseMessaging: FirebaseMessaging
) {
    private val log = LoggerFactory.getLogger(PushService::class.java)

    @Transactional
    fun subscribeAll(userId: UUID, token: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(messageUtil.resolveMessage("member.not.found"), HttpStatus.NOT_FOUND) }

        val alreadyExists = pushTokenRepository.findAllByUserId(user.id!!)
            .any { it.token == token }

        if (!alreadyExists) {
            pushTokenRepository.save(PushToken(user = user, token = token))
        }

        val bookmarkedMarkets = bookmarkRepository.findByUserId(user.id)
            .map { it.market.code }

        if (bookmarkedMarkets.isEmpty()) return

        runBlocking {
            bookmarkedMarkets.map { topic ->
                async {
                    firebaseMessaging.subscribeToTopic(listOf(token), topic)
                }
            }.awaitAll()
        }
    }

    @Transactional(readOnly = true)
    fun subscribe(userId: UUID, marketCode: String) {
        val tokens = pushTokenRepository.findAllByUserId(userId).map { it.token }
        if (tokens.isNotEmpty()) {
            firebaseMessaging.subscribeToTopic(tokens, marketCode)
        }
    }

    @Transactional(readOnly = true)
    fun unsubscribe(userId: UUID, marketCode: String) {
        val tokens = pushTokenRepository.findAllByUserId(userId).map { it.token }
        if (tokens.isNotEmpty()) {
            firebaseMessaging.unsubscribeFromTopic(tokens, marketCode)
        }
    }

    suspend fun sendAsync(title: String, body: String, topic: String) = withContext(Dispatchers.IO) {
        try {
            val message = Message.builder()
                .setTopic(topic)
                .putData("title", title)
                .putData("body", body)
                .build()

            firebaseMessaging.send(message)
        } catch (e: Exception) {
            log.error("FCM 전송 실패: ", e)
        }
    }
}
