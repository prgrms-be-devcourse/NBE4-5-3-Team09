package com.coing.domain.coin.ticker.service

import com.coing.domain.coin.common.port.CoinDataHandler
import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.coin.ticker.dto.TickerDto
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.domain.notification.entity.OneMinuteRate
import com.coing.domain.notification.service.PushService
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

@Service
class TickerService(
    private val messageUtil: MessageUtil,
    private val marketService: MarketService,
    private val eventPublisher: EventPublisher<TickerDto>,
    private val pushService: PushService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CoinDataHandler<Ticker> {
    private val tickerListCache: MutableMap<String, Deque<TickerDto>> = ConcurrentHashMap()
    private val lastSentTime = ConcurrentHashMap<String, Long>()
    private val lastPushSentTime = ConcurrentHashMap<String, Long>()
    private val pushMutexMap = ConcurrentHashMap<String, Mutex>()

    companion object {
        private const val THROTTLE_INTERVAL_MS = 200L
        private const val SLIDING_WINDOW_MS = 60_000L
        private const val PUSH_THROTTLE_INTERVAL_MS = 60_000L
    }

    override fun update(data: Ticker) {
        val code = data.code
        val dto = TickerDto.from(data, marketService.getCachedMarketByCode(code))
        updateTickerListCache(code, dto)
        publish(dto)
        pushMessage(dto)
    }

    override fun fallbackUpdate(lastUpdate: String) {
        tickerListCache.forEach { (code, deque) ->
            val latest = deque.peekLast()
            if (latest != null) {
                latest.isFallback = true
                latest.lastUpdate = lastUpdate
                publish(latest)
            }
        }

    }

    fun getTicker(market: String): TickerDto {
        val deque = tickerListCache[market]
        if (deque.isNullOrEmpty()) {
            throw BusinessException(messageUtil.resolveMessage("ticker.not.found"), HttpStatus.NOT_FOUND)
        }
        return deque.peekLast()
    }

    fun getTickers(markets: List<String>): List<TickerDto> =
        markets.map { getTicker(it) }

    private fun updateTickerListCache(code: String, dto: TickerDto) {
        val deque = tickerListCache.computeIfAbsent(code) { ConcurrentLinkedDeque() }

        synchronized(deque) {
            var lastOld: TickerDto? = null
            var current: TickerDto?
            val now = System.currentTimeMillis()
            val cutoff = now - SLIDING_WINDOW_MS

            while (true) {
                current = deque.pollFirst() ?: break
                if (current.timestamp < cutoff) {
                    lastOld = current
                } else {
                    deque.addFirst(current)
                    break
                }
            }
            lastOld?.let { deque.addFirst(it) }
            deque.addLast(dto)
        }
    }

    fun calculateOneMinuteRate(code: String, currentTradePrice: Double): Double {
        val deque = tickerListCache[code]
        if (deque.isNullOrEmpty()) return 0.0

        val target = System.currentTimeMillis() - SLIDING_WINDOW_MS

        return deque.filter { it.timestamp <= target }
            .maxByOrNull { it.timestamp }
            ?.let { (currentTradePrice - it.tradePrice) / it.tradePrice }
            ?: 0.0
    }

    fun pushMessage(dto: TickerDto) {
        val market = dto.code
        val now = System.currentTimeMillis()
        val mutex = pushMutexMap.computeIfAbsent(market) { Mutex() }

        coroutineScope.launch {
            mutex.withLock {
                val lastSent = lastPushSentTime[market] ?: 0L
                if (now - lastSent < PUSH_THROTTLE_INTERVAL_MS) return@withLock

                val absRate = kotlin.math.abs(dto.oneMinuteRate)
                val direction = if (dto.oneMinuteRate > 0) "HIGH" else "LOW"
                val messageKey = if (dto.oneMinuteRate > 0) "high.rate" else "low.rate"

                OneMinuteRate.entries
                    .filter { it != OneMinuteRate.NONE && absRate >= it.threshold }
                    .forEach { rate ->
                        val topic = "$market-$direction-${rate.name}"
                        val body = String.format(messageUtil.resolveMessage(messageKey), rate.threshold * 100)

                        pushService.sendAsync(
                            title = market,
                            body = body,
                            marketCode = market,
                            topic = topic
                        )
                    }

                lastPushSentTime[market] = now
            }
        }
    }

    fun publish(dto: TickerDto) {
        val market = dto.code
        val now = System.currentTimeMillis()
        val lastSent = lastSentTime[market] ?: 0L

        if (now - lastSent >= THROTTLE_INTERVAL_MS) {
            eventPublisher.publish(dto)
            lastSentTime[market] = now
        }
    }
}
