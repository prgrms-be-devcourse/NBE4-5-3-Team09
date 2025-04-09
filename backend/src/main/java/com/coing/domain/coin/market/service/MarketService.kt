package com.coing.domain.coin.market.service

import com.coing.domain.bookmark.repository.BookmarkRepository
import com.coing.domain.coin.market.dto.MarketResponseDto
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.port.MarketDataPort
import com.coing.domain.coin.market.repository.MarketRepository
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.coing.util.PageUtil
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class MarketService(
	private val messageUtil: MessageUtil,
	private val marketCacheService: MarketCacheService,
	private val bookmarkRepository: BookmarkRepository,
	private val marketRepository: MarketRepository,
    private val marketDataPort: MarketDataPort
) {

	private val log = LoggerFactory.getLogger(this::class.java)

	@Transactional
	@Scheduled(initialDelay = 0, fixedRate = 6 * 60 * 60 * 1000)
	fun updateMarketList() {
		val markets = fetchAndUpdateCoins()
		marketCacheService.updateMarketCache(markets)
	}

	private fun fetchAndUpdateCoins(): List<Market> {
		return try {
			val markets = marketDataPort.fetchMarkets()
			marketRepository.saveAll(markets)
			log.info("[Market] Market list updated.")
			markets
		} catch (e: Exception) {
			log.error("[Market] Error updating: ${e.message}. Falling back to DB.")
			marketRepository.findAll()
		}
	}

	fun getMarkets(pageable: Pageable): Page<Market> {
		val allMarkets = getCachedMarketList()
		return PageUtil.paginate(allMarkets, pageable)
	}

	fun getAllMarketsByQuote(
		principal: CustomUserPrincipal?,
		type: String,
		pageable: Pageable
	): Page<MarketResponseDto> {
		log.info("[Market] Get all market list by quote currency")

		val filtered = getCachedMarketList().filter { it.code.startsWith(type) }

		val bookmarkedMarkets: Set<String> = principal?.let {
			bookmarkRepository.findByUserIdAndQuote(it.id, type)
				.map { bookmark -> bookmark.market.code }
				.toSet()
		} ?: emptySet()

		val responseList = filtered.map { market ->
			MarketResponseDto.of(market, bookmarkedMarkets.contains(market.code))
		}

		return PageUtil.paginate(responseList, pageable)
	}

	@Transactional
	fun refreshMarketList() {
		log.info("[Market] Refresh market list")
		val markets = fetchAndUpdateCoins()
		marketCacheService.updateMarketCache(markets)
	}

	fun getMarketByUserAndCode(principal: CustomUserPrincipal?, code: String): MarketResponseDto {
		val cachedMarkets = getCachedMarketList()
		val isBookmarked = principal?.let {
			bookmarkRepository.existsByUserIdAndMarketCode(it.id, code)
		} ?: false

		return cachedMarkets.find { it.code == code }
			?.let { MarketResponseDto.of(it, isBookmarked) }
			?: throw BusinessException("Market not found", HttpStatus.NOT_FOUND)
	}

	fun getCachedMarketByCode(code: String): Market {
		return marketCacheService.getCachedMarketMap()[code]
			?: throw BusinessException(messageUtil.resolveMessage("market.not.found"), HttpStatus.NOT_FOUND)
	}

	fun getCachedMarketList(): List<Market> {
		return marketCacheService.getCachedMarketMap()
			.values
			.sortedBy { it.code }
	}
}