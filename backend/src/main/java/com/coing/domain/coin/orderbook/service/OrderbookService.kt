package com.coing.domain.coin.orderbook.service;

import com.coing.domain.coin.common.port.CoinDataHandler
import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.orderbook.dto.OrderbookDto
import com.coing.domain.coin.orderbook.entity.Orderbook
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderbookService(
    private val eventPublisher: EventPublisher<OrderbookDto>
	) : CoinDataHandler<Orderbook> {

	private val orderbookCache: MutableMap<String, OrderbookDto> = ConcurrentHashMap()
	private val lastSentTime: MutableMap<String, Long> = ConcurrentHashMap()

	override fun update(data: Orderbook) {
		val dto = OrderbookDto.from(data)
		orderbookCache[dto.code] = dto
		publish(dto)
	}

	fun publish(dto: OrderbookDto) {
		val market = dto.code
		val now = System.currentTimeMillis()
		val lastSent = lastSentTime.getOrDefault(market, 0L)

		if (now - lastSent >= THROTTLE_INTERVAL_MS) {
			eventPublisher.publish(dto)
			lastSentTime[market] = now
		}
	}

	fun getAllCachedData(): List<OrderbookDto> = orderbookCache.values.toList()

    override fun fallbackUpdate(lastUpdate: String) {
		orderbookCache.forEach { (code, dto) ->
			dto.isFallback = true
			dto.lastUpdate = lastUpdate
			publish(dto)
		}
    }

	@Scheduled(fixedRate = 60_000)
	fun cleanUp() {
		val expirationTime = System.currentTimeMillis() - 60_000
		orderbookCache.entries.removeIf { (market, _) ->
			lastSentTime.getOrDefault(market, 0L) < expirationTime
		}
	}

    companion object {
        private const val THROTTLE_INTERVAL_MS = 500L
        private val log = LoggerFactory.getLogger(OrderbookService::class.java)
    }
}
