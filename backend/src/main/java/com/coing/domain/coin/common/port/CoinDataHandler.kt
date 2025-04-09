package com.coing.domain.coin.common.port

interface CoinDataHandler<T> {
    fun update(data: T)
}