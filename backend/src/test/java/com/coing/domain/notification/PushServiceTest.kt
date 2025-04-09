package com.coing.domain.notification

import com.coing.domain.bookmark.entity.Bookmark
import com.coing.domain.bookmark.repository.BookmarkRepository
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.notification.entity.PushToken
import com.coing.domain.notification.repository.PushTokenRepository
import com.coing.domain.notification.service.PushService
import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import java.util.*

@ExtendWith(MockitoExtension::class)
class PushServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var pushTokenRepository: PushTokenRepository

    @Mock
    private lateinit var bookmarkRepository: BookmarkRepository

    @Mock
    private lateinit var messageUtil: MessageUtil

    @InjectMocks
    private lateinit var pushService: PushService

    @Mock
    private lateinit var firebaseMessaging: FirebaseMessaging

    private val userId = UUID.randomUUID()

    private fun getTestUser(): User {
        return User(
            id = userId,
            email = "test@example.com",
            password = "pass"
        )
    }

    private fun getTestMarket(): Market {
        return Market(
            code = "KRW-BTC",
            koreanName = "비트코인",
            englishName = "Bitcoin"
        )
    }

    private fun getTestBookmarks(user: User, market: Market): List<Bookmark> {
        return listOf(
            Bookmark(user = user, market = market, id = 1),
        )
    }

    private fun getToken() = "tokenA"
    private fun getTokens() = listOf("tokenA", "tokenB")

    @Test
    @DisplayName("subscribeAll - 존재하지 않는 유저 예외")
    fun subscribeAll_userNotFound_throwsException() {
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())
        `when`(messageUtil.resolveMessage("member.not.found")).thenReturn("User not found")

        val exception = assertThrows<BusinessException> {
            pushService.subscribeAll(userId, "dummy-token")
        }

        assertEquals("User not found", exception.message)
        assertEquals(HttpStatus.NOT_FOUND, exception.status)
    }

    @Test
    @DisplayName("subscribeAll - 성공적으로 토큰 저장 및 토픽 구독")
    fun subscribeAll_success() {
        val user = getTestUser()
        val token = getToken()
        val market = getTestMarket()
        val bookmarks = getTestBookmarks(user, market)

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(pushTokenRepository.findAllByUserId(userId)).thenReturn(emptyList())
        `when`(bookmarkRepository.findByUserId(userId)).thenReturn(bookmarks)

        pushService.subscribeAll(userId, token)

        verify(pushTokenRepository).save(any())
        verify(firebaseMessaging).subscribeToTopic(listOf(token), "KRW-BTC")
    }

    @Test
    @DisplayName("sendAsync - 예외 없이 성공")
    fun sendAsync_success() = runTest {
        `when`(firebaseMessaging.send(any(Message::class.java))).thenReturn("message-id")

        pushService.sendAsync("Title", "Body", "KRW-BTC")

        verify(firebaseMessaging).send(any(Message::class.java))
    }

    @Test
    @DisplayName("sendAsync - 예외 발생 시 로깅만")
    fun sendAsync_failure_logsError() = runTest {
        `when`(firebaseMessaging.send(any(Message::class.java))).thenThrow(RuntimeException("FCM 실패"))

        pushService.sendAsync("Title", "Body", "KRW-BTC")

        verify(firebaseMessaging).send(any(Message::class.java))
    }

    @Test
    @DisplayName("subscribe - 유저의 모든 토큰에 대해 토픽 구독")
    fun subscribe_success() {
        val tokens = getTokens()
        val user = getTestUser()
        val pushTokens = tokens.map { PushToken(user = user, token = it) }

        `when`(pushTokenRepository.findAllByUserId(userId)).thenReturn(pushTokens)

        pushService.subscribe(userId, "KRW-BTC")

        verify(firebaseMessaging).subscribeToTopic(tokens, "KRW-BTC")
    }

    @Test
    @DisplayName("unsubscribe - 유저의 모든 토큰에 대해 토픽 구독 해제")
    fun unsubscribe_success() {
        val tokens = getTokens()
        val user = getTestUser()
        val pushTokens = tokens.map { PushToken(user = user, token = it) }

        `when`(pushTokenRepository.findAllByUserId(userId)).thenReturn(pushTokens)

        pushService.unsubscribe(userId, "KRW-BTC")

        verify(firebaseMessaging).unsubscribeFromTopic(tokens, "KRW-BTC")
    }
}
