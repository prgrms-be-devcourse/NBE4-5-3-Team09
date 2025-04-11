package com.coing.domain.notification

import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.repository.MarketRepository
import com.coing.domain.notification.entity.OneMinuteRate
import com.coing.domain.notification.entity.PushToken
import com.coing.domain.notification.entity.Subscribe
import com.coing.domain.notification.entity.TradeImpact
import com.coing.domain.notification.repository.PushTokenRepository
import com.coing.domain.notification.repository.SubscribeRepository
import com.coing.domain.notification.service.PushService
import com.coing.domain.notification.service.SubscribeManager
import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
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
    private lateinit var marketRepository: MarketRepository

    @Mock
    private lateinit var pushTokenRepository: PushTokenRepository

    @Mock
    private lateinit var subscribeRepository: SubscribeRepository

    @Mock
    private lateinit var messageUtil: MessageUtil

    @Mock
    private lateinit var firebaseMessaging: FirebaseMessaging

    @Mock
    private lateinit var subscribeManager: SubscribeManager

    private lateinit var pushService: PushService

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)

    private val userId = UUID.randomUUID()

    private fun getTestUser(): User = User(id = userId, email = "test@example.com", password = "pass")

    private fun getTestMarket(): Market = Market(code = "KRW-BTC", koreanName = "비트코인", englishName = "Bitcoin")

    @BeforeEach
    fun setUp() {
        pushService = PushService(
            userRepository,
            marketRepository,
            pushTokenRepository,
            subscribeRepository,
            messageUtil,
            firebaseMessaging,
            subscribeManager,
            testScope
        )
    }

    @AfterEach
    fun clear() {
        testScope.cancel() // 테스트 종료 시 코루틴 정리
    }

    @Test
    @DisplayName("saveToken - 유저 없을 시 예외 발생")
    fun saveToken_userNotFound() {
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())
        `when`(messageUtil.resolveMessage("member.not.found")).thenReturn("User not found")

        val exception = assertThrows<BusinessException> {
            pushService.saveToken(userId, "token123")
        }

        assertEquals("User not found", exception.message)
        assertEquals(HttpStatus.NOT_FOUND, exception.status)
    }

    @Test
    @DisplayName("saveToken - 중복되지 않은 경우 저장")
    fun saveToken_success() {
        val user = getTestUser()
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(pushTokenRepository.findAllByUserId(userId)).thenReturn(emptyList())

        pushService.saveToken(userId, "token123")

        verify(pushTokenRepository).save(any())
    }

    @Test
    @DisplayName("updateSubscription - topic 구독 변경 및 DB 반영")
    fun updateSubscription_success() = runTest {
        val user = getTestUser()
        val market = getTestMarket()
        val tokens = listOf("tokenA", "tokenB")
        val oldRate = OneMinuteRate.THREE
        val newRate = OneMinuteRate.FIVE
        val oldImpact = TradeImpact.SLIGHT
        val newImpact = TradeImpact.MEDIUM

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        `when`(marketRepository.findByCode(market.code)).thenReturn(market)
        `when`(pushTokenRepository.findAllByUserId(userId)).thenReturn(
            tokens.map { token -> PushToken(user = user, token = token) }
        )
        `when`(subscribeRepository.findByUser_IdAndMarket_Code(userId, market.code)).thenReturn(null)

        pushService.updateSubscription(userId, market.code, newRate, oldRate, newImpact, oldImpact)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(subscribeRepository).save(any())
        verify(subscribeManager).updateTopicsAsync(tokens, market.code, newRate, oldRate, newImpact, oldImpact)
    }

    @Test
    @DisplayName("getSubscribeInfo - 구독 정보 조회")
    fun getSubscribeInfo_success() {
        val user = getTestUser()
        val market = getTestMarket()
        val subscribe = Subscribe(
            user = user,
            market = market,
            oneMinuteRate = OneMinuteRate.THREE,
            tradeImpact = TradeImpact.MEDIUM
        )

        `when`(subscribeRepository.findByUser_IdAndMarket_Code(userId, market.code)).thenReturn(subscribe)

        val result = pushService.getSubscribeInfo(userId, market.code)

        assertEquals(OneMinuteRate.THREE, result.oneMinuteRate)
        assertEquals(TradeImpact.MEDIUM, result.tradeImpact)
    }

    @Test
    @DisplayName("getSubscribeInfo - 구독 정보 없으면 NONE 반환")
    fun getSubscribeInfo_none() {
        `when`(subscribeRepository.findByUser_IdAndMarket_Code(userId, "KRW-BTC")).thenReturn(null)

        val result = pushService.getSubscribeInfo(userId, "KRW-BTC")

        assertEquals(OneMinuteRate.NONE, result.oneMinuteRate)
        assertEquals(TradeImpact.NONE, result.tradeImpact)
    }

    @Test
    @DisplayName("sendAsync - 메시지 정상 전송")
    fun sendAsync_success() = runTest {
        pushService.sendAsync("제목", "내용", "KRW-BTC", "KRW-BTC-HIGH-1")
        verify(firebaseMessaging).send(any())
    }

    @Test
    @DisplayName("sendAsync - 전송 실패시 예외 로그 출력")
    fun sendAsync_failure() = runTest {
        `when`(firebaseMessaging.send(any())).thenThrow(RuntimeException("전송 실패"))
        pushService.sendAsync("제목", "내용", "KRW-BTC", "KRW-BTC-HIGH-1")
        verify(firebaseMessaging).send(any())
    }
}
