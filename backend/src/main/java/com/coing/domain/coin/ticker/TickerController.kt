package com.coing.domain.coin.ticker

import com.coing.domain.coin.ticker.dto.TickerResponse
import com.coing.domain.coin.ticker.dto.TickersRequest
import com.coing.domain.coin.ticker.dto.TickersResponse
import com.coing.domain.coin.ticker.service.TickerService
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ticker")
@Tag(name = "Ticker API", description = "현재가 조회 관련 API 엔드포인트")
class TickerController(
    private val tickerService: TickerService
) {

    @Operation(summary = "특정 마켓 현재가 조회")
    @GetMapping("/{market}")
    @ApiErrorCodeExamples(ErrorCode.TICKER_NOT_FOUND)
    fun getTicker(@PathVariable("market") market: String): ResponseEntity<TickerResponse> {
        val response = TickerResponse.from(tickerService.getTicker(market))
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "여러 마켓 현재가 조회")
    @PostMapping
    fun getTickers(@RequestBody request: TickersRequest): ResponseEntity<TickersResponse> {
        val response = TickersResponse.from(tickerService.getTickers(request.markets))
        return ResponseEntity.ok(response)
    }
}
