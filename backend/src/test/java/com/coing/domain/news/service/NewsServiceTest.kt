package com.coing.domain.news.service

import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.repository.MarketRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NewsServiceTest @Autowired constructor(
    val newsService: NewsService,
    val marketRepository: MarketRepository
) {

    @BeforeEach
    fun setup() {
        if (!marketRepository.findById("KRW-BTC").isPresent) {
            val market = Market(
                code = "KRW-BTC",
                koreanName = "비트코인",
                englishName = "Bitcoin"
            )
            marketRepository.save(market)
        }
    }

    @Test
    fun `마켓 코드로 뉴스 조회`() {
        val marketCode = "KRW-BTC"
        val display = 100
        val start = 1
        val sort = "sim"
        val format = "json"

        val result = newsService.searchNewsByMarketCode(marketCode, display, start, sort, format)
        // 결과가 null이 아니며, 간단히 특정 키워드가 포함되었는지 확인 (구현에 따라 적절히 변경)
        assertNotNull(result)
        assertTrue(result.contains("news"), "응답에 news 키워드가 포함되어야 합니다.")
    }
}
