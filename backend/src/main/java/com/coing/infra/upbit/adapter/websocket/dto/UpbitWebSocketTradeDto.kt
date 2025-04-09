package com.coing.infra.upbit.adapter.websocket.dto

import com.coing.domain.coin.common.enums.AskBid
import com.coing.domain.coin.common.enums.Change
import com.coing.domain.coin.trade.entity.Trade
import com.coing.util.LocalDateDeserializer
import com.coing.util.LocalTimeDeserializer
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDate
import java.time.LocalTime

data class UpbitWebSocketTradeDto(

    @JsonProperty("ty")
    val type: String,

    @JsonProperty("cd")
    val code: String,

    @JsonProperty("tp")
    val tradePrice: Double,

    @JsonProperty("tv")
    val tradeVolume: Double,

    @JsonProperty("ab")
    val askBid: AskBid,

    @JsonProperty("pcp")
    val prevClosingPrice: Double,

    @JsonProperty("c")
    val change: Change,

    @JsonProperty("cp")
    val changePrice: Double,

    @JsonProperty("td")
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val tradeDate: LocalDate,

    @JsonProperty("ttm")
    @JsonDeserialize(using = LocalTimeDeserializer::class)
    val tradeTime: LocalTime,

    @JsonProperty("ttms")
    val tradeTimeStamp: Long,

    @JsonProperty("tms")
    val timestamp: Long,

    @JsonProperty("sid")
    val sequentialId: Long,

    @JsonProperty("bap")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val bestAskPrice: Double,

    @JsonProperty("bas")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val bestAskSize: Double,

    @JsonProperty("bbp")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val bestBidPrice: Double,

    @JsonProperty("bbs")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val bestBidSize: Double,

    @JsonProperty("st")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val streamType: String

) {
    fun toEntity(): Trade {
        return Trade(
            type = type,
            code = code,
            tradePrice = tradePrice,
            tradeVolume = tradeVolume,
            askBid = askBid,
            prevClosingPrice = prevClosingPrice,
            change = change,
            changePrice = changePrice,
            tradeDate = tradeDate,
            tradeTime = tradeTime,
            tradeTimeStamp = tradeTimeStamp,
            timestamp = timestamp,
            sequentialId = sequentialId,
            bestAskPrice = bestAskPrice,
            bestAskSize = bestAskSize,
            bestBidPrice = bestBidPrice,
            bestBidSize = bestBidSize
        )
    }
}
