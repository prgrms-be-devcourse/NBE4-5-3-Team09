package com.coing.domain.news.controller

import com.coing.domain.news.service.NewsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "News API", description = "뉴스 관련 API 엔드포인트")
@RestController
@RequestMapping("/api")
class NewsController(
	private val newsService: NewsService
) {

	/**
	 * 마켓 코드로 뉴스 조회 (검색어는 해당 마켓의 한국 이름 사용)
	 */
	@Operation(summary = "마켓 코드로 뉴스 조회 (검색어는 해당 마켓의 한국 이름 사용)")
	@GetMapping("/news")
	fun searchNews(
		@RequestParam(name = "market") marketCode: String,
		@RequestParam(name = "display", defaultValue = "100") display: Int,
		@RequestParam(name = "start", defaultValue = "1") start: Int,
		@RequestParam(name = "sort", defaultValue = "sim") sort: String,
		@RequestParam(name = "format", defaultValue = "json") format: String
	): ResponseEntity<String> {
		return try {
			val result = newsService.searchNewsByMarketCode(marketCode, display, start, sort, format)
			ResponseEntity.ok(result)
		} catch (ex: Exception) {
			// 로깅 등 추가 작업 후 사용자에게 에러 메시지를 응답합니다.
			ResponseEntity.status(503)
				.body("{\"error\": \"뉴스 서비스를 불러올 수 없습니다. 잠시 후 다시 시도해 주세요.\"}")
		}
	}
}
