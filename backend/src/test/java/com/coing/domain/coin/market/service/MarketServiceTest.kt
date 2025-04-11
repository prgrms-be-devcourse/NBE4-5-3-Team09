package com.coing.domain.coin.market.service

import com.coing.domain.bookmark.repository.BookmarkRepository
import com.coing.domain.coin.market.dto.MarketResponseDto
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.port.MarketDataPort
import com.coing.domain.coin.market.repository.MarketRepository
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import java.util.*

class MarketServiceTest {

	private lateinit var marketService: MarketService
	private lateinit var messageUtil: MessageUtil
	private lateinit var marketCacheService: MarketCacheService
	private lateinit var bookmarkRepository: BookmarkRepository
	private lateinit var marketRepository: MarketRepository
	private lateinit var marketDataPort: MarketDataPort

	private val testMarket = Market(
		code = "KRW-BTC",
		koreanName = "비트코인",
		englishName = "Bitcoin"
	)

	@BeforeEach
	fun setUp() {
		messageUtil = mock(MessageUtil::class.java)
		marketCacheService = mock(MarketCacheService::class.java)
		bookmarkRepository = mock(BookmarkRepository::class.java)
		marketRepository = mock(MarketRepository::class.java)
		marketDataPort = mock(MarketDataPort::class.java)

		marketService = MarketService(
			messageUtil,
			marketCacheService,
			bookmarkRepository,
			marketRepository,
			marketDataPort
		)
	}

	@Test
	fun `getCachedMarketByCode - ok`() {
		val cache = mapOf(testMarket.code to testMarket)
		`when`(marketCacheService.getCachedMarketMap()).thenReturn(cache)

		val result = marketService.getCachedMarketByCode("KRW-BTC")
		assertThat(result).isEqualTo(testMarket)
	}

	@Test
	fun `getCachedMarketByCode - exception when market not found`() {
		`when`(marketCacheService.getCachedMarketMap()).thenReturn(emptyMap())
		`when`(messageUtil.resolveMessage("market.not.found")).thenReturn("Market not found")

		val exception = catchThrowable {
			marketService.getCachedMarketByCode("KRW-BTC")
		}

		assertThat(exception).isInstanceOf(BusinessException::class.java)
		assertThat((exception as BusinessException).status).isEqualTo(HttpStatus.NOT_FOUND)
	}

	@Test
	fun `getMarketByUserAndCode - ok`() {
		val principal = mock(CustomUserPrincipal::class.java)
		val id = UUID.randomUUID()
		`when`(principal.id).thenReturn(id)
		`when`(bookmarkRepository.existsByUserIdAndMarketCode(id, "KRW-BTC")).thenReturn(true)
		`when`(marketCacheService.getCachedMarketMap()).thenReturn(mapOf("KRW-BTC" to testMarket))

		val result = marketService.getMarketByUserAndCode(principal, "KRW-BTC")
		assertThat(result).isInstanceOf(MarketResponseDto::class.java)
		assertThat(result.code).isEqualTo("KRW-BTC")
		assertThat(result.isBookmarked).isTrue()
	}

	@Test
	fun `getAllMarketsByQuote - filtering with bookmark`() {
		// given
		val principal = mock(CustomUserPrincipal::class.java)
		val id = UUID.randomUUID()
		`when`(principal.id).thenReturn(id)

		// 마켓 객체 mock
		val market = mock(Market::class.java)
		`when`(market.code).thenReturn("KRW-BTC")
		`when`(market.koreanName).thenReturn("비트코인")
		`when`(market.englishName).thenReturn("BTC")

		// 캐시에서 마켓 맵 반환
		`when`(marketCacheService.getCachedMarketMap()).thenReturn(
			mapOf("KRW-BTC" to market)
		)

		// 북마크 객체 mock
		val bookmark = mock(com.coing.domain.bookmark.entity.Bookmark::class.java)
		`when`(bookmark.market).thenReturn(market)

		`when`(bookmarkRepository.findByUserIdAndQuote(id, "KRW"))
			.thenReturn(listOf(bookmark))

		val pageable: Pageable = PageRequest.of(0, 10)

		// when
		val result = marketService.getAllMarketsByQuote(principal, "KRW", pageable)

		// then
		assertThat(result.content).hasSize(1)
		assertThat(result.content.first().isBookmarked).isTrue()
	}
}
