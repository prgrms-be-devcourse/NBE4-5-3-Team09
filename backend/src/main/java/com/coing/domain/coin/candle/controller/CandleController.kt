package com.coing.domain.coin.candle.controller

import com.coing.domain.coin.candle.dto.CandleDto
import com.coing.domain.coin.candle.service.UpbitCandleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/candles")
class CandleController(
    private val upbitCandleService: UpbitCandleService
) {

    // 예: /api/candles/KRW-BTC/seconds
    // 분봉의 경우 /api/candles/KRW-BTC/minutes?unit=5 (5분봉)
    @GetMapping("/{market}/{type}")
    fun getCandles(
        @PathVariable("market") market: String,
        @PathVariable("type") type: String,
        @RequestParam(value = "unit", required = false) unit: Int?
    ): ResponseEntity<List<CandleDto>> {
        val candles = upbitCandleService.getLatestCandles(market, type, unit)
        return ResponseEntity.ok(candles)
    }
}
