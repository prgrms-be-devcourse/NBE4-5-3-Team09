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

    override fun fetchMarkets(): List<Market>  {
        val response = restTemplate.getForEntity(
            UpbitApiEndpoints.MARKET_ALL,
            Array<UpbitApiMarketDto>::class.java
        )
        if (response.statusCode != HttpStatus.OK || response.body.isNullOrEmpty()) {
            log.warn("Failed Upbit Market API call: status=${response.statusCode}, body=null or empty")
            // 실패 시 빈 리스트 대신 예외를 던져서 상위에서 fallback을 처리하도록 함
            throw RuntimeException("Market API call failed with status: ${response.statusCode}")
        }
        return response.body!!.map { it.toEntity() }
    }
}