package com.coing.domain.chat.controller

import com.coing.domain.chat.entity.ChatMessage
import com.coing.domain.chat.service.ChatReportService
import com.coing.domain.chat.service.ChatService
import com.coing.domain.user.entity.User
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import com.coing.util.BasicResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat/messages")
class ChatReportController(
    private val chatService: ChatService,
    private val chatReportService: ChatReportService
) {

    /**
     * 신고 API
     *
     * 경로: /api/chat/messages/{messageId}/report
     * HTTP 메서드: POST
     *
     * @param messageId 신고할 메시지의 ID
     * @param currentUser 현재 인증된 사용자 (신고자)
     */
    @Operation(summary = "메세지 신고하기")
    @ApiErrorCodeExamples(
        ErrorCode.MESSAGE_NOT_FOUND,
        ErrorCode.MESSAGE_ALREADY_REPORTED,
        ErrorCode.MEMBER_NOT_FOUND,
        ErrorCode.MESSAGE_REPORT_FAILED,
        ErrorCode.INTERNAL_SERVER_ERROR)
    @PostMapping("/{messageId}/report")
    fun reportMessage(
        @PathVariable messageId: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<BasicResponse> {
        val message: ChatMessage = chatService.findMessageById(messageId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BasicResponse(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다.", ""))

        return try {
            // 신고를 처리하고 신고 내역을 DB에 저장
            chatReportService.reportMessage(message, currentUser)
            ResponseEntity.ok(BasicResponse(HttpStatus.OK, "메시지가 신고되었습니다.", ""))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BasicResponse(HttpStatus.BAD_REQUEST, e.message ?: "신고에 실패했습니다.", ""))
        }
    }

}
