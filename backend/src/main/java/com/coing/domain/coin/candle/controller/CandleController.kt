package com.coing.domain.coin.candle.controller

import com.coing.domain.coin.candle.controller.dto.CandleResponse
import com.coing.domain.coin.candle.enums.EnumCandleType
import com.coing.domain.coin.candle.service.UpbitCandleService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Candle API", description = "캔들 차트 관련 API 엔드포인트")
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
        @PathVariable("type") type: EnumCandleType,
        @RequestParam(value = "unit", required = false) unit: Int?
    ): ResponseEntity<List<CandleResponse>> {
        val candles = upbitCandleService.getLatestCandles(market, type, unit)
        val response = candles.map { CandleResponse.from(it) }
        return ResponseEntity.ok(response)
    }
}
