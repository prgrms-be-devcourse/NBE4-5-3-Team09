package com.coing.infra.upbit.dto

import com.coing.domain.coin.orderbook.entity.Orderbook
import com.coing.domain.coin.orderbook.entity.OrderbookUnit
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Upbit WebSocket Orderbook(호가) Response Dto
 * format field : "SIMPLE"로 지정하여 응답의 필드명이 모두 간소화함
 */
data class UpbitWebSocketOrderbookDto(
    @JsonProperty("ty")
    val type: String,

    @JsonProperty("cd")
    val code: String,

    @JsonProperty("tas")
    val totalAskSize: Double,

    @JsonProperty("tbs")
    val totalBidSize: Double,

    @JsonProperty("obu")
    val orderbookUnits: List<OrderbookUnitDto>? = null,

    @JsonProperty("tms")
    val timestamp: Long,

    @JsonProperty("lv")
    val level: Double,

    @JsonProperty("st")
    val streamType: String
) {

    fun toEntity(): Orderbook {
        return Orderbook(
            type = type,
            code = code,
            totalAskSize = totalAskSize,
            totalBidSize = totalBidSize,
            timestamp = timestamp,
            level = level,
            orderbookUnits = orderbookUnits?.map { it.toEntity() }?.toMutableList() ?: mutableListOf()
        )
    }

    data class OrderbookUnitDto(
        @JsonProperty("ap")
        val askPrice: Double,

        @JsonProperty("bp")
        val bidPrice: Double,

        @JsonProperty("as")
        val askSize: Double,

        @JsonProperty("bs")
        val bidSize: Double
    ) {
        fun toEntity(): OrderbookUnit {
            return OrderbookUnit(askPrice, bidPrice, askSize, bidSize)
        }
    }
}
