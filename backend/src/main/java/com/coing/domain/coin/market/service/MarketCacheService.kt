package com.coing.domain.coin.market.service

import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.repository.MarketRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class MarketCacheService(
	private val marketRepository: MarketRepository
) {

	private val log = LoggerFactory.getLogger(MarketCacheService::class.java)

	@CachePut(value = ["markets"])
	fun updateMarketCache(markets: List<Market>): Map<String, Market> {
		log.info("[MarketCacheService] Updating cache with {} markets", markets.size)
		return markets.associateBy { it.code }
	}

	@Cacheable(value = ["markets"])
	fun getCachedMarketMap(): Map<String, Market> {
		return marketRepository.findAll().associateBy { it.code }
	}
}