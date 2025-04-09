package com.coing.domain.bookmark.controller

import com.coing.domain.bookmark.controller.dto.BookmarkRequest
import com.coing.domain.bookmark.controller.dto.BookmarkResponse
import com.coing.domain.bookmark.service.BookmarkService
import com.coing.domain.coin.common.dto.PagedResponse
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import com.coing.util.BasicResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "BookMark API", description = "북마크 관련 API 엔드포인트")
@RestController
@RequestMapping("/api")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {

    @Operation(summary = "북마크 등록", security = [SecurityRequirement(name = "bearerAuth")])
    @PostMapping("/bookmark")
    @ApiErrorCodeExamples(ErrorCode.BOOKMARK_NOT_FOUND, ErrorCode.MEMBER_NOT_FOUND, ErrorCode.BOOKMARK_ALREADY_EXISTS)
    fun addBookmark(
        @RequestBody @Validated request: BookmarkRequest,
        @AuthenticationPrincipal principal: CustomUserPrincipal
    ): ResponseEntity<BasicResponse> {
        bookmarkService.addBookmark(request, principal)
        val response = BasicResponse(HttpStatus.CREATED, "북마크 등록 성공", "")
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "유저 북마크 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @GetMapping("/bookmarks/{quote}")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND)
    fun getBookmarksByQuote(
        @AuthenticationPrincipal principal: CustomUserPrincipal,
        @PathVariable quote: String,
        @ParameterObject @PageableDefault(size = 9) pageable: Pageable
    ): ResponseEntity<PagedResponse<BookmarkResponse>> {
        val result = bookmarkService.getBookmarksByQuote(principal, quote, pageable)
        val response = PagedResponse(
            result.number,
            result.size,
            result.totalElements,
            result.totalPages,
            result.content
        )
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "특정 북마크 삭제", security = [SecurityRequirement(name = "bearerAuth")])
    @DeleteMapping("/bookmark/{marketCode}")
    @ApiErrorCodeExamples(ErrorCode.BOOKMARK_ACCESS_DENIED, ErrorCode.BOOKMARK_NOT_FOUND)
    fun deleteBookmark(
        @PathVariable marketCode: String,
        @AuthenticationPrincipal principal: CustomUserPrincipal
    ): ResponseEntity<BasicResponse> {
        bookmarkService.deleteBookmark(principal.id, marketCode)
        val response = BasicResponse(HttpStatus.NO_CONTENT, "북마크 삭제 성공", "")
        return ResponseEntity.ok(response)
    }
}
