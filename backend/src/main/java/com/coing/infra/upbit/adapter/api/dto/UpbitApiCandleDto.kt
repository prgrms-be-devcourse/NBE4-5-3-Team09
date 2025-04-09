package com.coing.infra.upbit.adapter.api.dto

import com.coing.domain.coin.candle.entity.Candle
import com.fasterxml.jackson.annotation.JsonProperty

data class UpbitApiCandleDto(
    @JsonProperty("market") val code: String? = null,
    @JsonProperty("candle_date_time_utc") val candleDateTimeUtc: String? = null,
    @JsonProperty("opening_price") val open: Double = 0.0,
    @JsonProperty("high_price") val high: Double = 0.0,
    @JsonProperty("low_price") val low: Double = 0.0,
    @JsonProperty("trade_price") val close: Double = 0.0,
    @JsonProperty("candle_acc_trade_volume") val volume: Double = 0.0,
    @JsonProperty("timestamp") val timestamp: Long = 0L
) {
    fun toEntity(): Candle = Candle(
        code = code ?: "",
        candleDateTimeUtc = candleDateTimeUtc ?: "",
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume,
        timestamp = timestamp
    )
}
