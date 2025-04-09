package com.coing.domain.notification.controller

import com.coing.domain.notification.service.PushService
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/push")
class PushController(
    private val pushService: PushService
) {
    data class SubscribeRequest(val token: String)

    @PostMapping("/subscribe")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND)
    fun subscribe(
        @RequestBody request: SubscribeRequest,
        @AuthenticationPrincipal principal: CustomUserPrincipal
    ): ResponseEntity<Void> {
        pushService.subscribeAll(principal.id, request.token)
        return ResponseEntity.ok().build()
    }
}
