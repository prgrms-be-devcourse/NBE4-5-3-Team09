package com.coing.infra.upbit.adapter.websocket.dto

import com.coing.domain.coin.common.enums.AskBid
import com.coing.domain.coin.common.enums.Change
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.domain.coin.ticker.entity.enums.MarketState
import com.coing.domain.coin.ticker.entity.enums.MarketWarning
import com.coing.util.LocalDateDeserializer
import com.coing.util.LocalTimeDeserializer
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDate
import java.time.LocalTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class UpbitWebSocketTickerDto(
    @JsonProperty("ty")
    val type: String,

    @JsonProperty("cd")
    val code: String,

    @JsonProperty("op")
    val openingPrice: Double,

    @JsonProperty("hp")
    val highPrice: Double,

    @JsonProperty("lp")
    val lowPrice: Double,

    @JsonProperty("tp")
    val tradePrice: Double,

    @JsonProperty("pcp")
    val prevClosingPrice: Double,

    @JsonProperty("c")
    val change: Change,

    @JsonProperty("cp")
    val changePrice: Double,

    @JsonProperty("scp")
    val signedChangePrice: Double,

    @JsonProperty("cr")
    val changeRate: Double,

    @JsonProperty("scr")
    val signedChangeRate: Double,

    @JsonProperty("tv")
    val tradeVolume: Double,

    @JsonProperty("atv")
    val accTradeVolume: Double,

    @JsonProperty("atv24h")
    val accTradeVolume24h: Double,

    @JsonProperty("atp")
    val accTradePrice: Double,

    @JsonProperty("atp24h")
    val accTradePrice24h: Double,

    @JsonProperty("tdt")
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val tradeDate: LocalDate,

    @JsonProperty("ttm")
    @JsonDeserialize(using = LocalTimeDeserializer::class)
    val tradeTime: LocalTime,

    @JsonProperty("ttms")
    val tradeTimestamp: Long,

    @JsonProperty("ab")
    val askBid: AskBid,

    @JsonProperty("aav")
    val accAskVolume: Double,

    @JsonProperty("abv")
    val accBidVolume: Double,

    @JsonProperty("h52wp")
    val highest52WeekPrice: Double,

    @JsonProperty("h52wdt")
    val highest52WeekDate: LocalDate,

    @JsonProperty("l52wp")
    val lowest52WeekPrice: Double,

    @JsonProperty("l52wdt")
    val lowest52WeekDate: LocalDate,

    @JsonProperty("ms")
    val marketState: MarketState,

    @JsonProperty("mw")
    val marketWarning: MarketWarning,

    @JsonProperty("tms")
    val timestamp: Long,

    @JsonProperty("st")
    val streamType: String
) {
    fun toEntity(): Ticker = Ticker(
        type = type,
        code = code,
        openingPrice = openingPrice,
        highPrice = highPrice,
        lowPrice = lowPrice,
        tradePrice = tradePrice,
        prevClosingPrice = prevClosingPrice,
        change = change,
        changePrice = changePrice,
        signedChangePrice = signedChangePrice,
        changeRate = changeRate,
        signedChangeRate = signedChangeRate,
        tradeVolume = tradeVolume,
        accTradeVolume = accTradeVolume,
        accTradeVolume24h = accTradeVolume24h,
        accTradePrice = accTradePrice,
        accTradePrice24h = accTradePrice24h,
        tradeDate = tradeDate,
        tradeTime = tradeTime,
        tradeTimestamp = tradeTimestamp,
        askBid = askBid,
        accAskVolume = accAskVolume,
        accBidVolume = accBidVolume,
        highest52WeekPrice = highest52WeekPrice,
        highest52WeekDate = highest52WeekDate,
        lowest52WeekPrice = lowest52WeekPrice,
        lowest52WeekDate = lowest52WeekDate,
        marketState = marketState,
        marketWarning = marketWarning,
        timestamp = timestamp,
        accAskBidRate = calcAccAskBidRate(),
        highBreakout = calcHighBreakout(),
        lowBreakout = calcLowBreakout()
    )

    private fun calcAccAskBidRate(): Double {
        return if (accTradeVolume != 0.0) {
            (accAskVolume - accBidVolume) / accTradeVolume
        } else {
            0.0
        }
    }

    private fun calcHighBreakout(): Boolean {
        return tradePrice > highest52WeekPrice
    }

    private fun calcLowBreakout(): Boolean {
        return tradePrice < lowest52WeekPrice
    }
}
