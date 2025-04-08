package com.coing.domain.coin.orderbook.service;

import com.coing.domain.coin.common.port.DataHandler
import com.coing.domain.coin.orderbook.dto.OrderbookDto
import com.coing.domain.coin.orderbook.entity.Orderbook
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderbookService(
	private val messagingTemplate: SimpMessageSendingOperations
	) : DataHandler<Orderbook> {

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
			messagingTemplate.convertAndSend("/sub/coin/orderbook/$market", dto)
			lastSentTime[market] = now
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
