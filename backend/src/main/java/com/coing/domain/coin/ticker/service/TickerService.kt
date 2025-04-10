package com.coing.domain.coin.ticker.service

import com.coing.domain.coin.common.port.CoinDataHandler
import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.coin.ticker.dto.TickerDto
import com.coing.domain.coin.ticker.entity.Ticker
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
    private val pushService: PushService
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

        // 락 가져오기 (없으면 새로 생성)
        val mutex = pushMutexMap.computeIfAbsent(market) { Mutex() }

        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val lastSent = lastPushSentTime[market] ?: 0L
                if (now - lastSent < PUSH_THROTTLE_INTERVAL_MS) return@withLock

                val thresholds = listOf(
                    0.01 to "1",
                    0.03 to "3",
                    0.05 to "5",
                    0.10 to "10"
                )

                thresholds.forEach { (threshold, label) ->
                    when {
                        dto.oneMinuteRate >= threshold -> {
                            pushService.sendAsync(
                                title = "${dto.koreanName} ${dto.englishName}",
                                body = String.format(messageUtil.resolveMessage("high.rate"), label),
                                marketCode = market,
                                topic = "$market-HIGH-$label"
                            )
                        }

                        dto.oneMinuteRate <= -threshold -> {
                            pushService.sendAsync(
                                title = "${dto.koreanName} ${dto.englishName}",
                                body = String.format(messageUtil.resolveMessage("low.rate"), label),
                                marketCode = market,
                                topic = "$market-LOW-$label"
                            )
                        }
                    }
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
