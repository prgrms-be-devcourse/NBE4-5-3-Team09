package com.coing.domain.coin.ticker.dto

import jakarta.validation.constraints.NotNull

data class TickersRequest(
    @field:NotNull val markets: List<String>
)
