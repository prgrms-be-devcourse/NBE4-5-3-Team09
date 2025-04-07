package com.coing.domain.coin.trade.entity

import com.coing.domain.coin.common.enums.AskBid
import com.coing.domain.coin.common.enums.Change
import java.time.LocalDate
import java.time.LocalTime

data class Trade(
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
    val bestBidSize: Double
)
