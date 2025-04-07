package com.coing.domain.coin.ticker.dto

import com.coing.domain.coin.common.enums.AskBid
import com.coing.domain.coin.common.enums.Change
import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.ticker.entity.Ticker
import com.coing.domain.coin.ticker.entity.enums.MarketState
import com.coing.domain.coin.ticker.entity.enums.MarketWarning
import java.time.LocalDate
import java.time.LocalTime

data class TickerDto(
    val type: String,             // 데이터 타입 (예: "ticker")
    val code: String,             // 마켓 코드 (예: "KRW-BTC")
//    val koreanName: String,       // 한글 이름
//    val englishName: String,      // 영어 이름
    val openingPrice: Double,     // 시가
    val highPrice: Double,        // 고가
    val lowPrice: Double,         // 저가
    val tradePrice: Double,       // 현재가
    val prevClosingPrice: Double, // 전일 종가
    val change: Change,           // 전일 대비
    val changePrice: Double,      // 전일 대비 값
    val signedChangePrice: Double,// 전일 대비 값 (부호 포함)
    val changeRate: Double,       // 전일 대비 변동률
    val signedChangeRate: Double, // 전일 대비 변동률 (부호 포함)
    val tradeVolume: Double,      // 가장 최근 거래량
    val accTradeVolume: Double,   // 누적 거래량
    val accTradeVolume24h: Double,// 24시간 누적 거래량
    val accTradePrice: Double,    // 누적 거래대금
    val accTradePrice24h: Double, // 24시간 누적 거래대금
    val tradeDate: LocalDate,     // 최근 거래 일자
    val tradeTime: LocalTime,     // 최근 거래 시각
    val tradeTimestamp: Long,     // 체결 타임스탬프
    val askBid: AskBid,           // 매수/매도 구분
    val accAskVolume: Double,     // 누적 매도량
    val accBidVolume: Double,     // 누적 매수량
    val highest52WeekPrice: Double, // 52주 최고가
    val highest52WeekDate: LocalDate, // 52주 최고가 달성일
    val lowest52WeekPrice: Double, // 52주 최저가
    val lowest52WeekDate: LocalDate, // 52주 최저가 달성일
    val marketState: MarketState,    // 거래 상태
    val marketWarning: MarketWarning,// 유의 종목 여부
    val timestamp: Long,             // 타임스탬프

    // 계산된 지표
    val accAskBidRate: Double,   // 매수/매도 누적 비율
    val highBreakout: Boolean,   // 52주 최고가 갱신 여부
    val lowBreakout: Boolean     // 52주 최저가 갱신 여부
    // val oneMinuteRate: Double? // 주석 처리됨
) {
    companion object {
        fun from(ticker: Ticker, market: Market): TickerDto {
            return TickerDto(
                type = ticker.type,
                code = ticker.code,
//                koreanName = market.koreanName,
//                englishName = market.englishName,
                openingPrice = ticker.openingPrice,
                highPrice = ticker.highPrice,
                lowPrice = ticker.lowPrice,
                tradePrice = ticker.tradePrice,
                prevClosingPrice = ticker.prevClosingPrice,
                change = ticker.change,
                changePrice = ticker.changePrice,
                signedChangePrice = ticker.signedChangePrice,
                changeRate = ticker.changeRate,
                signedChangeRate = ticker.signedChangeRate,
                tradeVolume = ticker.tradeVolume,
                accTradeVolume = ticker.accTradeVolume,
                accTradeVolume24h = ticker.accTradeVolume24h,
                accTradePrice = ticker.accTradePrice,
                accTradePrice24h = ticker.accTradePrice24h,
                tradeDate = ticker.tradeDate,
                tradeTime = ticker.tradeTime,
                tradeTimestamp = ticker.tradeTimestamp,
                askBid = ticker.askBid,
                accAskVolume = ticker.accAskVolume,
                accBidVolume = ticker.accBidVolume,
                highest52WeekPrice = ticker.highest52WeekPrice,
                highest52WeekDate = ticker.highest52WeekDate,
                lowest52WeekPrice = ticker.lowest52WeekPrice,
                lowest52WeekDate = ticker.lowest52WeekDate,
                marketState = ticker.marketState,
                marketWarning = ticker.marketWarning,
                timestamp = ticker.timestamp,
                accAskBidRate = ticker.accAskBidRate,
                highBreakout = ticker.highBreakout,
                lowBreakout = ticker.lowBreakout
                // oneMinuteRate = ticker.oneMinuteRate // 주석 처리됨
            )
        }
    }
}
