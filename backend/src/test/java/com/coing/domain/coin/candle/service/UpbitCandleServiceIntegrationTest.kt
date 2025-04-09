package com.coing.domain.coin.candle.service

import com.coing.domain.coin.candle.enums.EnumCandleType
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UpbitCandleServiceIntegrationTest {

    @Autowired
    lateinit var upbitCandleService: UpbitCandleService

    @Test
    fun `실제 API 호출 - seconds 캔들 데이터 조회`() {
        // 실제 API를 호출합니다. (테스트 환경에 따라 실패할 수 있음)
        val candles = upbitCandleService.getLatestCandles("KRW-BTC", EnumCandleType.seconds, null)
        // candles가 null이 아니고, 리스트로 반환되어야 함
        assertNotNull(candles)
        println("실제 API로부터 ${candles.size}개의 캔들 데이터를 조회함.")
    }

    @Test
    fun `실제 API 호출 - minutes 캔들 데이터 조회`() {
        val candles = upbitCandleService.getLatestCandles("KRW-BTC", EnumCandleType.minutes, 1)
        assertNotNull(candles)
        println("실제 API로부터 ${candles.size}개의 캔들 데이터를 조회함.")
    }
}