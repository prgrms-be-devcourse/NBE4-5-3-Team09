package com.coing.domain.coin.candle.entity

data class Candle(
    val code: String,
    val candleDateTimeUtc: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val timestamp: Long
)
