package com.coing.domain.user.controller

import com.coing.domain.chat.dto.ChatMessageReportDto
import com.coing.domain.chat.service.ChatReportService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val chatReportService: ChatReportService
) {

    // ADMIN 권한만 접근할 수 있도록 설정
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/reported-messages")
    fun getReportedMessages(): ResponseEntity<List<ChatMessageReportDto>> {
        val reportedMessages = chatReportService.getReportedMessages(3)
        return ResponseEntity.ok(reportedMessages)
    }
}
