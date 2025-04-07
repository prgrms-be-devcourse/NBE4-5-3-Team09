package com.coing.domain.coin.candle.dto

import com.coing.global.annotation.NoArg
import com.fasterxml.jackson.annotation.JsonProperty

@NoArg
data class CandleDto(
	@JsonProperty("market")
	var code: String? = null,

	@JsonProperty("candle_date_time_utc")
	var candleDateTimeUtc: String? = null,

	@JsonProperty("opening_price")
	var open: Double = 0.0,

	@JsonProperty("high_price")
	var high: Double = 0.0,

	@JsonProperty("low_price")
	var low: Double = 0.0,

	@JsonProperty("trade_price")
	var close: Double = 0.0,

	@JsonProperty("candle_acc_trade_volume")
	var volume: Double = 0.0,

	@JsonProperty("timestamp")
	var timestamp: Long = 0L
)
