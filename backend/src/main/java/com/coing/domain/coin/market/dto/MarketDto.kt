package com.coing.domain.coin.market.dto

import com.coing.domain.coin.market.entity.Market
import com.fasterxml.jackson.annotation.JsonProperty

data class MarketDto(
	@JsonProperty("market")
	val market: String,

	@JsonProperty("korean_name")
	val koreanName: String,

	@JsonProperty("english_name")
	val englishName: String
) {
	fun toEntity(): Market {
		return Market(
			code = market,
			koreanName = koreanName,
			englishName = englishName
		)
	}
}