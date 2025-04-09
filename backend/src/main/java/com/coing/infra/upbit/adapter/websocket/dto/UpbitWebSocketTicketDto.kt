package com.coing.infra.upbit.adapter.websocket.dto

import jakarta.validation.constraints.NotBlank

/**
 * Upbit WebSocket Request Ticket Field
 * 일반적으로 용도를 식별하기 위해 ticket 이라는 필드값이 필요하다.
 * 이 값은 시세를 수신하는 대상을 식별하며 되도록 유니크한 값을 사용하도록 권장한다. (UUID 등)
 */
data class UpbitWebSocketTicketDto(
    @field:NotBlank
    var ticket: String
)
