package com.coing.domain.coin.ticker.dto

data class TickerResponse(val ticker: TickerDto) {
    companion object {
        fun from(ticker: TickerDto): TickerResponse = TickerResponse(ticker)
    }
}
