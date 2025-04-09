package com.coing.domain.coin.ticker.dto

data class TickersResponse(val tickers: List<TickerDto>) {
    companion object {
        fun from(tickers: List<TickerDto>): TickersResponse = TickersResponse(tickers)
    }
}
