package com.coing.domain.coin.common.port

interface EventPublisher<T> {
    fun publish(event: T)
}