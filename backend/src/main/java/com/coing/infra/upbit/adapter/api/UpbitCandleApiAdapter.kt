package com.coing.infra.upbit.adapter.api

import com.coing.domain.coin.candle.entity.Candle
import com.coing.domain.coin.candle.enums.EnumCandleType
import com.coing.domain.coin.candle.port.CandleDataPort
import com.coing.infra.upbit.adapter.api.constant.UpbitApiEndpoints
import com.coing.infra.upbit.adapter.api.dto.UpbitApiCandleDto
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@Profile("!test")
class UpbitCandleApiAdapter(
    private val restTemplate: RestTemplate
) : CandleDataPort {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun fetchLatestCandles(market: String, candleType: EnumCandleType, unit: Int?): List<Candle> {
        val url = buildUrl(market, candleType, unit)
        val response = restTemplate.getForEntity(url, Array<UpbitApiCandleDto>::class.java)
        if (response.statusCode != HttpStatus.OK || response.body.isNullOrEmpty()) {
            log.warn("Failed Upbit Candle API call: status=${response.statusCode}")
            throw RuntimeException("Candle API call failed with status: ${response.statusCode}")
        }
        val candleList = response.body!!.map { it.toEntity() }.toMutableList()
        candleList.reverse()
        return candleList
    }

    private fun buildUrl(market: String, candleType: EnumCandleType, unit: Int?): String = when(candleType) {
        EnumCandleType.seconds ->
            "${UpbitApiEndpoints.CANDLES_SECONDS}?market=${market}&count=200"
        EnumCandleType.minutes ->
            "${UpbitApiEndpoints.CANDLES_MINUTES}/${unit?:1}?market=${market}&count=200"
        EnumCandleType.days ->
            "${UpbitApiEndpoints.CANDLES_DAYS}?market=${market}&count=200"
        EnumCandleType.weeks ->
            "${UpbitApiEndpoints.CANDLES_WEEKS}?market=${market}&count=200"
        EnumCandleType.months ->
            "${UpbitApiEndpoints.CANDLES_MONTHS}?market=${market}&count=200"
        EnumCandleType.years ->
            "${UpbitApiEndpoints.CANDLES_YEARS}?market=${market}&count=200"
    }

}