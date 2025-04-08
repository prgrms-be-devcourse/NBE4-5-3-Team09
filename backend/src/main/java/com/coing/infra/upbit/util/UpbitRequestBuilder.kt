package com.coing.infra.upbit.util

import com.coing.domain.coin.market.service.MarketCacheService
import com.coing.infra.upbit.dto.UpbitWebSocketFormatDto
import com.coing.infra.upbit.dto.UpbitWebSocketTicketDto
import com.coing.infra.upbit.dto.UpbitWebSocketTypeDto
import com.coing.infra.upbit.enums.EnumUpbitRequestType
import com.coing.infra.upbit.enums.EnumUpbitWebSocketFormat
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.*

@Component
class UpbitRequestBuilder(
    private val marketCacheService: MarketCacheService,
    private val objectMapper: ObjectMapper = ObjectMapper()
) {

    @Throws(JsonProcessingException::class)
    fun makeRequest(type: EnumUpbitRequestType): String {
        val codes = marketCacheService.getCachedMarketMap().keys.toList()
        val ticketDto = UpbitWebSocketTicketDto(
            ticket = type.value
        )
        val typeDto = UpbitWebSocketTypeDto(
            type = type.value,
            codes = codes,
            isOnlyRealtime = false,
            isOnlySnapshot = false
        )
        val formatDto = UpbitWebSocketFormatDto(
            format = EnumUpbitWebSocketFormat.SIMPLE
        )

        val dataList = listOf(ticketDto, typeDto, formatDto)
        return objectMapper.writeValueAsString(dataList)
    }
}
