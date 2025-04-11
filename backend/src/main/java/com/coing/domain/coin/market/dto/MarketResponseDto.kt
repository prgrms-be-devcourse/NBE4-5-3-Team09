package com.coing.domain.coin.market.dto

import com.coing.domain.coin.market.entity.Market

data class MarketResponseDto(
	val code: String,
	val koreanName: String,
	val englishName: String,
	val isBookmarked: Boolean
) {
	companion object {
		fun of(market: Market, isBookmarked: Boolean): MarketResponseDto {
			return MarketResponseDto(
				code = market.code,
				koreanName = market.koreanName,
				englishName = market.englishName,
				isBookmarked = isBookmarked
			)
		}
	}
}