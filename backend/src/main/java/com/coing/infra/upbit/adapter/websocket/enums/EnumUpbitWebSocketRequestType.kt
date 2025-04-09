package com.coing.infra.upbit.adapter.websocket.enums

/**
 * Upbit WebSocket Type
 */
enum class EnumUpbitWebSocketRequestType(val value: String) {
    TICKER("ticker"),
    TRADE("trade"),
    ORDERBOOK("orderbook"),
    CANDLE("candle")
}
