package com.coing.domain.coin.orderbook.entity

data class OrderbookUnit(
    val askPrice: Double, // 매도 호가
    val bidPrice: Double, // 매수 호가
    val askSize: Double, // 매도 잔량
    val bidSize: Double, // 매수 잔량
)