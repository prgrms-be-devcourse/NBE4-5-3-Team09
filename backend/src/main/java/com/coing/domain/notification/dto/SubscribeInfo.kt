package com.coing.domain.notification.dto

import com.coing.domain.notification.entity.OneMinuteRate
import com.coing.domain.notification.entity.TradeImpact

data class SubscribeInfo(
    val oneMinuteRate: OneMinuteRate = OneMinuteRate.NONE,
    val tradeImpact: TradeImpact = TradeImpact.NONE
)
