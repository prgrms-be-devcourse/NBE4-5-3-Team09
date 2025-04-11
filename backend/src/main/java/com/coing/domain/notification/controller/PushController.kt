package com.coing.domain.notification.controller

import com.coing.domain.notification.dto.PushMessage
import com.coing.domain.notification.dto.PushTokenSaveRequest
import com.coing.domain.notification.dto.SubscribeInfo
import com.coing.domain.notification.dto.SubscribeRequest
import com.coing.domain.notification.service.PushService
import com.coing.domain.user.dto.CustomUserPrincipal
import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/push")
@Tag(name = "Push API", description = "Push 알림 API 엔드포인트")
class PushController(
    private val pushService: PushService
) {
    @PostMapping("/register")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND)
    fun subscribe(
        @RequestBody request: PushTokenSaveRequest,
        @AuthenticationPrincipal principal: CustomUserPrincipal
    ): ResponseEntity<Void> {
        pushService.saveToken(principal.id, request.token)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/subscribe")
    @ApiErrorCodeExamples(ErrorCode.MEMBER_NOT_FOUND, ErrorCode.MARKET_NOT_FOUND)
    fun updateSubscription(
        @RequestBody request: SubscribeRequest,
        @AuthenticationPrincipal principal: CustomUserPrincipal
    ): ResponseEntity<Void> {
        pushService.updateSubscription(
            userId = principal.id,
            marketCode = request.market,
            newRate = request.subscribeInfo.oneMinuteRate,
            oldRate = request.unsubscribeInfo.oneMinuteRate,
            newImpact = request.subscribeInfo.tradeImpact,
            oldImpact = request.unsubscribeInfo.tradeImpact
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping("/subscribe-info")
    fun getSubscribeInfo(
        @RequestParam market: String,
        @AuthenticationPrincipal principal: CustomUserPrincipal
    ): ResponseEntity<SubscribeInfo> {
        val info = pushService.getSubscribeInfo(principal.id, market)
        return ResponseEntity.ok(info)
    }

    @PostMapping("/test")
    fun test(
        @RequestBody request: PushMessage,
    ): ResponseEntity<Void> {
        CoroutineScope(Dispatchers.IO).launch {
            pushService.sendAsync(request.title, request.body, "KRW-BTC", "KRW-BTC-HIGH-ONE")
        }
        return ResponseEntity.ok().build()
    }
}
