package com.coing.domain.coin.trade.service

import com.coing.domain.coin.common.port.CoinDataHandler
import com.coing.domain.coin.common.port.EventPublisher
import com.coing.domain.coin.trade.dto.TradeDto
import com.coing.domain.coin.trade.entity.Trade
import com.coing.domain.notification.entity.TradeImpact
import com.coing.domain.notification.service.PushService
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Service
@Slf4j
class TradeService(
    private val eventPublisher: EventPublisher<TradeDto>,
    private val messageUtil: MessageUtil,
    private val pushService: PushService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CoinDataHandler<Trade> {

    private val tradeListCache = ConcurrentHashMap<String, ConcurrentLinkedQueue<TradeDto>>()
    private val vwapCache = ConcurrentHashMap<String, Double>()
    private val totalTradeVolumeCache = ConcurrentHashMap<String, Double>()
    private val totalTradeNumberCache = ConcurrentHashMap<String, Double>()
    private val prevTradePriceCache = ConcurrentHashMap<String, Double>()
    private val lastSentTime = ConcurrentHashMap<String, Long>()
    private val lastPushSentTime = ConcurrentHashMap<String, Long>()
    private val pushMutexMap = ConcurrentHashMap<String, Mutex>()

    companion object {
        private const val THROTTLE_INTERVAL_MS = 200L
        private const val MAX_LIST_SIZE = 20
        private const val PUSH_THROTTLE_INTERVAL_MS = 60_000L
    }

    override fun update(data: Trade) {
        val market = data.code
        val price = data.tradePrice
        val volume = data.tradeVolume

        // 캐시 갱신
        updateCaches(market, volume)

        // 지표 계산
        val vwap = calculateVWAP(market, price, volume)
        val averageTradeSize = calculateAverageTradeSize(market)
        val tradeImpact = calculateTradeImpact(market, price)

        val dto = TradeDto.of(data, vwap, averageTradeSize, tradeImpact)

        updateTradeListCache(market, dto)
        publish(dto)
        pushMessage(dto)
    }

    private fun updateTradeListCache(market: String, dto: TradeDto) {
        tradeListCache.compute(market) { _, queue ->
            val newQueue = queue ?: ConcurrentLinkedQueue<TradeDto>()
            newQueue.add(dto)
            while (newQueue.size > MAX_LIST_SIZE) newQueue.poll()
            newQueue
        }
    }

    fun getTrades(market: String): List<TradeDto> {
        val queue = tradeListCache[market]
        if (queue.isNullOrEmpty()) {
            throw BusinessException(
                messageUtil.resolveMessage("trade.not.found"),
                HttpStatus.NOT_FOUND
            )
        }
        return queue.toList()
    }

    fun pushMessage(dto: TradeDto) {
        val market = dto.code
        val now = System.currentTimeMillis()
        val mutex = pushMutexMap.computeIfAbsent(market) { Mutex() }

        coroutineScope.launch {
            mutex.withLock {
                val lastSent = lastPushSentTime[market] ?: 0L
                if (now - lastSent < PUSH_THROTTLE_INTERVAL_MS) return@withLock

                val absImpact = kotlin.math.abs(dto.tradeImpact)
                val direction = if (dto.tradeImpact > 0) "HIGH" else "LOW"
                val messageKey = if (dto.tradeImpact > 0) "high.trade" else "low.trade"

                TradeImpact.entries
                    .filter { it != TradeImpact.NONE && absImpact >= it.threshold }
                    .forEach { impact ->
                        val topic = "$market-$direction-${impact.name}"
                        val body = String.format(messageUtil.resolveMessage(messageKey), impact.threshold)

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

    fun publish(dto: TradeDto) {
        val market = dto.code
        val now = System.currentTimeMillis()
        val lastSent = lastSentTime[market] ?: 0L

        if (now - lastSent >= THROTTLE_INTERVAL_MS) {
            eventPublisher.publish(dto)
            lastSentTime[market] = now
        }
    }

    override fun fallbackUpdate(lastUpdate: String) {
        tradeListCache.forEach { (code, queue) ->
            val latest = queue.lastOrNull()
            if (latest != null) {
                latest.isFallback = true
                latest.lastUpdate = lastUpdate
                publish(latest)
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun resetCaches() {
        vwapCache.clear()
        totalTradeVolumeCache.clear()
        totalTradeNumberCache.clear()
        prevTradePriceCache.clear()
    }

    private fun updateCaches(market: String, volume: Double) {
        vwapCache.putIfAbsent(market, 0.0)
        prevTradePriceCache.putIfAbsent(market, 0.0)
        totalTradeVolumeCache.merge(market, volume) { old, new -> old + new }
        totalTradeNumberCache.merge(market, 1.0) { old, new -> old + new }
    }

    /**
     * VWAP(Volume-Weighted Average Price) 체결가 가중 평균 가격 계산
     */
    private fun calculateVWAP(market: String, price: Double, volume: Double): Double {
        val newTotalVolume = totalTradeVolumeCache[market] ?: volume
        val previousVolume = newTotalVolume - volume
        val previousVWAP = if (previousVolume <= 0.0) price else (vwapCache[market] ?: price)

        // VWAP = (이전 VWAP*이전 거래량 + 현재 거래가격*현재 거래량) / (이전 거래량 + 현재 거래량)
        val newVWAP = ((previousVWAP * previousVolume) + (price * volume)) / newTotalVolume

        vwapCache[market] = newVWAP
        return newVWAP
    }

    /**
     * 평균 거래 크기 계산
     */
    private fun calculateAverageTradeSize(market: String): Double {
        val totalVolume = totalTradeVolumeCache[market] ?: 0.0
        val totalNumber = totalTradeNumberCache[market] ?: 0.0
        return if (totalNumber == 0.0) 0.0 else totalVolume / totalNumber
    }

    /**
     * 거래 임팩트 계산
     */
    private fun calculateTradeImpact(market: String, price: Double): Double {
        val prevPrice = prevTradePriceCache[market] ?: 0.0
        prevTradePriceCache[market] = price
        return if (prevPrice == 0.0) 0.0 else (price - prevPrice) / prevPrice * 100
    }
}
