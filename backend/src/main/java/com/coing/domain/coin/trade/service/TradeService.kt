package com.coing.domain.coin.trade.service

import com.coing.domain.coin.trade.dto.TradeDto
import com.coing.domain.coin.trade.entity.Trade
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Service
@Slf4j
class TradeService(
    private val simpMessageSendingOperations: SimpMessageSendingOperations,
    private val messageUtil: MessageUtil
) {

    private val tradeListCache = ConcurrentHashMap<String, ConcurrentLinkedQueue<TradeDto>>()
    private val vwapCache = ConcurrentHashMap<String, Double>()
    private val totalTradeVolumeCache = ConcurrentHashMap<String, Double>()
    private val totalTradeNumberCache = ConcurrentHashMap<String, Double>()
    private val prevTradePriceCache = ConcurrentHashMap<String, Double>()
    private val lastSentTime = ConcurrentHashMap<String, Long>()

    private val throttleIntervalMs = 200L
    private val maxListSize = 20

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

    fun updateTrade(trade: Trade) {
        val market = trade.code
        val price = trade.tradePrice
        val volume = trade.tradeVolume

        // 캐시 갱신
        cacheValues(market, volume)

        val vwap = calculateVWAP(market, price, volume)
        val averageTradeSize = calculateAverageTradeSize(market)
        val tradeImpact = calculateTradeImpact(market, price)

        val dto = TradeDto.of(trade, vwap, averageTradeSize, tradeImpact)

        publish(dto)

        tradeListCache.compute(market) { _, queue ->
            val newQueue = queue ?: ConcurrentLinkedQueue()
            newQueue.add(dto)
            while (newQueue.size > maxListSize) newQueue.poll()
            newQueue
        }
    }

    fun publish(dto: TradeDto) {
        val market = dto.code
        val now = System.currentTimeMillis()
        val lastSent = lastSentTime[market] ?: 0L

        if (now - lastSent >= throttleIntervalMs) {
            simpMessageSendingOperations.convertAndSend("/sub/coin/trade/$market", dto)
            lastSentTime[market] = now
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun resetCaches() {
        vwapCache.clear()
        totalTradeVolumeCache.clear()
        totalTradeNumberCache.clear()
        prevTradePriceCache.clear()
    }

    private fun cacheValues(market: String, volume: Double) {
        vwapCache.putIfAbsent(market, 0.0)
        totalTradeVolumeCache.putIfAbsent(market, 0.0)
        totalTradeNumberCache.putIfAbsent(market, 0.0)
        prevTradePriceCache.putIfAbsent(market, 0.0)

        totalTradeVolumeCache[market] = totalTradeVolumeCache[market]!! + volume
        totalTradeNumberCache[market] = totalTradeNumberCache[market]!! + 1
    }

    private fun calculateVWAP(market: String, price: Double, volume: Double): Double {
        val prevVWAP = vwapCache[market] ?: 0.0
        val totalVolume = totalTradeVolumeCache[market] ?: 0.0
        return if (totalVolume == 0.0) 0.0 else ((prevVWAP * totalVolume) + (price * volume)) / (totalVolume + volume)
    }

    private fun calculateAverageTradeSize(market: String): Double {
        val totalVolume = totalTradeVolumeCache[market] ?: 0.0
        val totalNumber = totalTradeNumberCache[market] ?: 0.0
        return if (totalNumber == 0.0) 0.0 else totalVolume / totalNumber
    }

    private fun calculateTradeImpact(market: String, price: Double): Double {
        val prevPrice = prevTradePriceCache[market] ?: 0.0
        prevTradePriceCache[market] = price
        return if (prevPrice == 0.0) 0.0 else (price - prevPrice) / prevPrice * 100
    }
}
