package com.coing.domain.notification.dto

data class SubscribeRequest(
    val market: String,
    val subscribeInfo: SubscribeInfo,
    val unsubscribeInfo: SubscribeInfo
)
