package com.coing.domain.coin.candle.port

import com.coing.domain.coin.candle.entity.Candle
import com.coing.domain.coin.candle.enums.EnumCandleType

interface CandleDataPort {
    fun fetchLatestCandles(market: String, candleType: EnumCandleType, unit: Int?): List<Candle>
}