package com.coing.infra.upbit.enums

/**
 * Upbit WebSocket Type
 */
enum class EnumUpbitRequestType(val value: String) {
    TICKER("ticker"),
    TRADE("trade"),
    ORDERBOOK("orderbook"),
    CANDLE("candle")
}
