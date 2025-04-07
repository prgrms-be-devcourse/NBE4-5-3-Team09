package com.coing.domain.coin.market.controller.dto

import com.coing.domain.coin.market.dto.MarketResponseDto
import jakarta.validation.constraints.NotNull

data class MarketResponse(
	@field:NotNull
	val code: String,

	@field:NotNull
	val koreanName: String,

	@field:NotNull
	val englishName: String,

	val isBookmarked: Boolean
) {
	companion object {
		fun from(market: MarketResponseDto): MarketResponse {
			return MarketResponse(
				code = market.code,
				koreanName = market.koreanName,
				englishName = market.englishName,
				isBookmarked = market.isBookmarked
			)
		}
	}
}