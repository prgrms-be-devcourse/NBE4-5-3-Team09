package com.coing.domain.user.controller

import com.coing.domain.chat.dto.ChatMessageReportDto
import com.coing.domain.chat.service.ChatReportService
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "관리자 관련 API 엔드포인트")
class AdminController(
    private val chatReportService: ChatReportService
) {

    // 관리자 권한 확인용 엔드포인트
    @Operation(summary = "관리자 권한 확인", description = "현재 로그인한 사용자가 관리자 권한을 가지고 있는지 확인합니다.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/auth-check")
    fun checkAdminAuthority(): ResponseEntity<Map<String, String>> {
        // 단순 메시지 반환 등 추가 정보도 전달할 수 있음.
        return ResponseEntity.ok(mapOf("status" to "success", "message" to "관리자 권한이 확인되었습니다."))
    }

    // ADMIN 권한만 접근할 수 있도록 설정
    @Operation(summary = "신고된 메시지 조회", description = "신고된 메시지를 조회합니다.")
    @ApiErrorCodeExamples(
        ErrorCode.REPORT_NOT_FOUND,
        ErrorCode.INVALID_TOKEN,
        ErrorCode.TOKEN_REQUIRED,
        ErrorCode.INTERNAL_SERVER_ERROR
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/reported-messages")
    fun getReportedMessages(): ResponseEntity<List<ChatMessageReportDto>> {
        val reportedMessages = chatReportService.getReportedMessages()
        return ResponseEntity.ok(reportedMessages)
    }
}
