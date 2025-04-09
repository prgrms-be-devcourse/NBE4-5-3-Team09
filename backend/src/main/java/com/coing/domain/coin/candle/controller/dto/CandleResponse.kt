package com.coing.domain.coin.candle.controller.dto

import com.coing.domain.coin.candle.entity.Candle
import jakarta.validation.constraints.NotNull

data class CandleResponse(
	@field:NotNull val market: String,
	@field:NotNull val candleDateTimeUtc: String,
	@field:NotNull val openingPrice: Double,
	@field:NotNull val highPrice: Double,
	@field:NotNull val lowPrice: Double,
	@field:NotNull val tradePrice: Double,
	@field:NotNull val candleAccTradeVolume: Double,
	@field:NotNull val timestamp: Long,
) {
	companion object {
		fun from(candle: Candle): CandleResponse {
			return CandleResponse(
                market = candle.code,
                candleDateTimeUtc = candle.candleDateTimeUtc,
                openingPrice = candle.open,
                highPrice = candle.high,
                lowPrice = candle.low,
                tradePrice = candle.close,
                candleAccTradeVolume = candle.volume,
                timestamp = candle.timestamp
			)
		}
	}
}