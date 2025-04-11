package com.coing.domain.coin.orderbook.dto

import com.coing.domain.coin.common.port.CodeDto
import com.coing.domain.coin.orderbook.entity.Orderbook
import com.coing.domain.coin.orderbook.entity.OrderbookUnit

data class OrderbookDto(
    val type: String,                        // "orderbook"
    override val code: String,                        // 마켓 코드 (ex. KRW-BTC)
    val totalAskSize: Double,                // 호가 매도 총 잔량
    val totalBidSize: Double,                // 호가 매수 총 잔량
    val orderbookUnits: List<OrderbookUnit>, // 상세 호가 정보 목록
    val timestamp: Long,                     // 타임스탬프 (millisecond)
    val level: Double,                       // 호가 모아보기 단위 (default:0)
    val midPrice: Double,                    // 중간 가격
    val spread: Double,                      // 매도/매수 호가 차이
    val imbalance: Double,                   // 잔량 불균형
    val liquidityDepth: Double,               // ±X% 유동성 비율
    var isFallback: Boolean = false,
    var lastUpdate: String? = null
) : CodeDto{
    companion object {
        fun from(orderbook: Orderbook): OrderbookDto = OrderbookDto(
            type = orderbook.type,
            code = orderbook.code,
            totalAskSize = orderbook.totalAskSize,
            totalBidSize = orderbook.totalBidSize,
            orderbookUnits = orderbook.orderbookUnits,
            timestamp = orderbook.timestamp,
            level = orderbook.level,
            midPrice = orderbook.midPrice,
            spread = orderbook.spread,
            imbalance = orderbook.imbalance,
            liquidityDepth = orderbook.liquidityDepth
        )
    }
}
