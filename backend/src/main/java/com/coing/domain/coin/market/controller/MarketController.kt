package com.coing.domain.coin.market.controller

import com.coing.domain.coin.common.dto.PagedResponse
import com.coing.domain.coin.market.controller.dto.MarketResponse
import com.coing.domain.coin.market.service.MarketService
import com.coing.domain.user.dto.CustomUserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotNull
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/market")
@Tag(name = "Market API", description = "종목 조회 관련 API 엔드포인트")
class MarketController(
	private val marketService: MarketService
) {

	@Operation(summary = "북마크한 특정 마켓 정보 조회")
	@GetMapping("/{code}")
	fun getMarketByUserAndCode(
		@AuthenticationPrincipal principal: CustomUserPrincipal,
		@PathVariable("code") code: String
	): ResponseEntity<MarketResponse> {
		val response = MarketResponse.from(marketService.getMarketByUserAndCode(principal, code))
		return ResponseEntity.ok(response)
	}

	@Operation(summary = "기준 통화별 마켓 전체 조회")
	@GetMapping
	fun getMarketsByQuote(
		@AuthenticationPrincipal principal: CustomUserPrincipal,
		@RequestParam("type") type: String,
		@ParameterObject @PageableDefault(sort = ["code"]) pageable: Pageable
	): ResponseEntity<@NotNull PagedResponse<MarketResponse>> {

		val page: Page<MarketResponse> = marketService.getAllMarketsByQuote(principal, type, pageable)
			.map { MarketResponse.from(it) }

		val response = PagedResponse(
			page.number,
			page.size,
			page.totalElements,
			page.totalPages,
			page.content
		)

		return ResponseEntity.ok(response)
	}

	@Operation(summary = "새로고침 요청")
	@PostMapping("/refresh")
	fun refreshMarkets(): ResponseEntity<Void> {
		marketService.refreshMarketList()
		return ResponseEntity.ok().build()
	}
}