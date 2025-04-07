package com.coing.domain.coin.trade.dto

data class TradeResponse(
    val trades: List<TradeDto>
) {
    companion object {
        fun from(trades: List<TradeDto>): TradeResponse = TradeResponse(trades)
    }
}
