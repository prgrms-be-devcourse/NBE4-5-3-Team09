package com.coing.domain.coin.ticker.service

import com.coing.domain.coin.common.port.DataHandler
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.coin.ticker.dto.TickerDto
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class TickerService(
    private val messageUtil: MessageUtil,
    private val marketService: MarketService,
    private val messagingTemplate: SimpMessageSendingOperations,
) : DataHandler<Ticker> {
    private val tickerCache = ConcurrentHashMap<String, TickerDto>()
    private val lastSentTime = ConcurrentHashMap<String, Long>()
    private val throttleIntervalMs = 200L

    override fun update(data: Ticker) {
        val market = marketService.getCachedMarketByCode(data.code)
        val dto = TickerDto.from(data, market)
        tickerCache[dto.code] = dto
        publish(dto)
    }

    fun getTicker(market: String): TickerDto {
        return tickerCache[market] ?: throw BusinessException(
            messageUtil.resolveMessage("ticker.not.found"), HttpStatus.NOT_FOUND
        )
    }

    fun getTickers(markets: List<String>): List<TickerDto> {
        return tickerCache.filterKeys { it in markets }.values.toList()
    }

    fun publish(dto: TickerDto) {
        val market = dto.code
        val now = System.currentTimeMillis()
        val lastSent = lastSentTime[market] ?: 0L

        if (now - lastSent >= throttleIntervalMs) {
            messagingTemplate.convertAndSend("/sub/coin/ticker/$market", dto)
            lastSentTime[market] = now
        }
    }
}
