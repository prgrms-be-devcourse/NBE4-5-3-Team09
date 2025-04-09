package com.coing.infra.upbit.adapter.api

import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.port.MarketDataPort
import com.coing.infra.upbit.adapter.api.constant.UpbitApiEndpoints
import com.coing.infra.upbit.adapter.api.dto.UpbitApiMarketDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class UpbitMarketApiAdapter(
    private val restTemplate: RestTemplate
) : MarketDataPort {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun fetchMarkets(): List<Market>  = try {
        val response = restTemplate.getForEntity(
            UpbitApiEndpoints.MARKET_ALL,
            Array<UpbitApiMarketDto>::class.java
        )

        if (response.statusCode != HttpStatus.OK || response.body.isNullOrEmpty()) {
            log.warn("Failed Upbit Market API call: status=${response.statusCode}, body=null or empty")
            emptyList()
        } else {
            response.body!!.map { it.toEntity() }
        }
    } catch (e: Exception) {
        log.error("[Market] Error fetching market data from Upbit: ${e.message}", e)
        emptyList()
    }
}