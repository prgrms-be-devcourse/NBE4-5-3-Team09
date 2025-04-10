package com.coing.domain.notification.dto

import com.coing.domain.notification.entity.OneMinuteRate

data class SubscribeInfo(
    val oneMinuteRate: OneMinuteRate = OneMinuteRate.NONE
)
