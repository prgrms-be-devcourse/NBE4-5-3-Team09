package com.coing.domain.coin.market.port

import com.coing.domain.coin.market.entity.Market

interface MarketDataPort {
    fun fetchMarkets(): List<Market>
}