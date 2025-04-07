package com.coing.domain.coin.orderbook.entity

/**
 * Orderbook Entity
 */
data class Orderbook (
    val type: String, // "orderbook"
    val code: String, // 마켓 코드 (ex. KRW-BTC)
    val totalAskSize: Double, // 호가 매도 총 잔량
    val totalBidSize: Double, // 호가 매수 총 잔량
    val orderbookUnits: List<OrderbookUnit>, // 상세 호가 정보 목록
    val timestamp: Long, // 타임스탬프 (millisecond)
    val level: Double // 호가 모아보기 단위 (default:0)
    ) {
    data class OrderbookBestPrices(
        val bestAskPrice: Double,
        val bestBidPrice: Double
    )

    companion object {
        private const val DEFAULT_RANGE_PERCENT = 0.01 // ±1%
    }

    val bestPrices: OrderbookBestPrices
        /**
         * 최우선 매도/매수 호가 계산
         *
         *
         * orderbookUnit이 오름차순으로 정렬되어 있다고 가정함.
         */
        get() {
            if (orderbookUnits.isEmpty()) return OrderbookBestPrices(0.0, 0.0)
            val bestAsk: Double = orderbookUnits.first().askPrice
            val bestBid: Double = orderbookUnits.first().bidPrice

            return OrderbookBestPrices(bestAsk, bestBid)
        }

    val spread: Double
        /**
         * 스프레드(Spread)
         *
         *
         * 최우선 매도 호가와 최우선 매수 호가의 가격 차이.
         * Spread = (BestAskPrice) − (BestBidPrice)
         * 시장의 유동성과 매도/매수 간 괴리를 파악.
         *
         * @return 계산된 스프레드 값. 데이터가 없을 경우 0.0 반환.
         */
        get() {
            val (ask, bid) = bestPrices
            return ask - bid
        }

    val imbalance: Double
        /**
         * 잔량 불균형 (Imbalance)
         *
         *
         * 매도 잔량과 매수 잔량의 비율
         * 매도 우위 시장인지, 매수 우위 시장인지 파악 가능.
         * Imbalance = (TotalAskSize - TotalBidSize) / (TotalAskSize + TotalBidSize)
         * 값 범위 : -1 ~ 1
         * 1에 가까울수록 완전히 매수 잔량에 쏠림, -1에 가까울수록 매도 잔량에 쏠림.
         */
        get() {
            val totalAsk = totalAskSize
            val totalBid = totalBidSize
            return if (totalAsk + totalBid == 0.0) 0.0 else (totalBid - totalAsk) / (totalAsk + totalBid)
        }

    val midPrice: Double
        /**
         * 중간 가격 (MidPrice)
         *
         *
         * 최우선 매도 호가와 최우선 매수 호가의 평균 값.
         * MidPrice = (BestAskPrice + BestBidPrice) / 2
         *
         * @return 계산된 중간 가격. 데이터가 없을 경우 0.0 반환.
         */
        get() {
            val (ask, bid) = bestPrices
            return (ask + bid) / 2.0
        }

    val liquidityDepth: Double
        /**
         * 호가 깊이 비율 계산 (Liquidity Depth %)
         *
         *
         * 특정 범위(DEFAULT_RANGE_PERCENT) 내 유동성이 전체 유동성 대비 몇 %인지 계산.
         * @return 해당 범위 내 유동성 비율 (0~100%)
         */
        get() {
            if (orderbookUnits.isEmpty()) return 0.0
            val midPrice = midPrice
            val lowerBound = midPrice * (1.0 - DEFAULT_RANGE_PERCENT)
            val upperBound = midPrice * (1.0 + DEFAULT_RANGE_PERCENT)

            var liquidityInRange = 0.0
            var totalLiquidity = 0.0

            orderbookUnits.forEach { unit ->
                if (unit.askPrice in lowerBound .. upperBound) liquidityInRange += unit.askSize
                if (unit.bidPrice in lowerBound .. upperBound) liquidityInRange += unit.bidSize
                totalLiquidity += unit.askSize + unit.bidSize
            }
            return if (totalLiquidity > 0) (liquidityInRange / totalLiquidity) * 100.0 else 0.0
        }
}