package com.coing.domain.notification.entity

enum class TradeImpact(val threshold: Double) {
    NONE(0.0), // 알림 받지 않음
    SLIGHT(0.1), // 0.1% 이상
    MEDIUM(0.5), // 0.5% 이상
    STRONG(1.0); // 1% 이상
}
