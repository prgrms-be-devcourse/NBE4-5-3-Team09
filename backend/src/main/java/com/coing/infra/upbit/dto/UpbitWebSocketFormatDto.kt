package com.coing.infra.upbit.dto

import com.coing.infra.upbit.enums.EnumUpbitWebSocketFormat

/**
 * Upbit WebSocket Request Type Field
 * 수신하고 싶은 시세 정보를 나열하는 필드
 * `is_only_snapshot`, `is_only_realtime` 필드는 생략 가능하며 모두 생략시 스냅샷과 실시간 데이터 모두를 수신한다.
 * 하나의 요청에 `{type field}`는 여러개를 명시할 수 있다.
 */
data class UpbitWebSocketFormatDto(
    var format: EnumUpbitWebSocketFormat
)
