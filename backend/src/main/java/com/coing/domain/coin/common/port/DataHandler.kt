package com.coing.domain.coin.common.port

interface DataHandler<T> {
    fun update(data: T)
}