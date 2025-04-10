package com.coing.domain.notification.entity

enum class OneMinuteRate {
    NONE, ONE, THREE, FIVE, TEN;

    fun toLevel(): String = when (this) {
        ONE -> "1"
        THREE -> "3"
        FIVE -> "5"
        TEN -> "10"
        NONE -> throw IllegalArgumentException("NONE은 topic으로 변환할 수 없습니다.")
    }
}
