package com.coing.domain.notification.entity

enum class OneMinuteRate(val threshold: Double) {
    NONE(0.0), // 알림 받지 않음
    ONE(0.01), // 0.01 이상
    THREE(0.03), // 0.03 이상
    FIVE(0.05), // 0.05 이상
    TEN(0.1); // 0.1 이상
}
