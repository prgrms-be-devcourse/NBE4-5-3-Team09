package com.coing.domain.coin.trade.dto

import com.coing.domain.coin.common.enums.AskBid
import com.coing.domain.coin.common.enums.Change
import com.coing.domain.coin.trade.entity.Trade
import java.time.LocalDate
import java.time.LocalTime

data class TradeDto(
    val type: String,
    val code: String,
    val tradePrice: Double,
    val tradeVolume: Double,
    val askBid: AskBid,
    val prevClosingPrice: Double,
    val change: Change,
    val changePrice: Double,
    val tradeDate: LocalDate,
    val tradeTime: LocalTime,
    val tradeTimeStamp: Long,
    val timestamp: Long,
    val sequentialId: Long,
    val bestAskPrice: Double,
    val bestAskSize: Double,
    val bestBidPrice: Double,
    val bestBidSize: Double,
    val vwap: Double,
    val averageTradeSize: Double,
    val tradeImpact: Double
) {
    companion object {
        fun of(trade: Trade, vwap: Double, averageTradeSize: Double, tradeImpact: Double): TradeDto =
            TradeDto(
                type = trade.type,
                code = trade.code,
                tradePrice = trade.tradePrice,
                tradeVolume = trade.tradeVolume,
                askBid = trade.askBid,
                prevClosingPrice = trade.prevClosingPrice,
                change = trade.change,
                changePrice = trade.changePrice,
                tradeDate = trade.tradeDate,
                tradeTime = trade.tradeTime,
                tradeTimeStamp = trade.tradeTimeStamp,
                timestamp = trade.timestamp,
                sequentialId = trade.sequentialId,
                bestAskPrice = trade.bestAskPrice,
                bestAskSize = trade.bestAskSize,
                bestBidPrice = trade.bestBidPrice,
                bestBidSize = trade.bestBidSize,
                vwap = vwap,
                averageTradeSize = averageTradeSize,
                tradeImpact = tradeImpact
            )
    }
}
