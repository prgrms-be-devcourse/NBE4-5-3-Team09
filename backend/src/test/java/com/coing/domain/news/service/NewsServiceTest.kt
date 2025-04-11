package com.coing.domain.news.service

import com.coing.domain.coin.market.entity.Market
import com.coing.domain.coin.market.repository.MarketRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean

@SpringBootTest
class NewsServiceTest @Autowired constructor(
    @SpyBean
    val newsService: NewsService,
    val marketRepository: MarketRepository
) {

    @BeforeEach
    fun setup() {
        // 테스트를 위해 KRW-BTC 마켓이 없으면 미리 생성
        if (!marketRepository.findById("KRW-BTC").isPresent) {
            val market = Market(
                code = "KRW-BTC",
                koreanName = "비트코인",
                englishName = "Bitcoin"
            )
            marketRepository.save(market)
        }
    }

    @AfterEach
    fun cleanupMocks() {
        reset(newsService) // mock 초기화
    }

    @Test
    fun `마켓 코드로 뉴스 조회`() {
        val marketCode = "KRW-BTC"
        val display = 100
        val start = 1
        val sort = "sim"
        val format = "json"

        val result = newsService.searchNewsByMarketCode(marketCode, display, start, sort, format)
        // 결과가 null이 아니며, 간단히 특정 키워드 (예제에선 "news")가 포함되었는지 확인합니다.
        assertNotNull(result)
        assertTrue(result.contains("news"), "응답에 news 키워드가 포함되어야 합니다.")
    }

    @Test
    fun `리트라이 로직 테스트 - executeGet 실패 후 최종 성공`() {
        // 시뮬레이션:
        // 첫 번째 호출: 예외 발생 ("Simulated Failure 1")
        // 두 번째 호출: 예외 발생 ("Simulated Failure 2")
        // 세 번째 호출: 성공 응답 반환
        doThrow(RuntimeException("Simulated Failure 1"))
            .doThrow(RuntimeException("Simulated Failure 2"))
            .doReturn("{\"news\": \"리트라이 테스트 성공\"}")
            .`when`(newsService).executeGet(anyString(), anyMap())

        val result = newsService.searchNews("리트라이 테스트", 10, 1, "sim", "json")
        assertNotNull(result)
        assertTrue(result.contains("리트라이 테스트 성공"), "리트라이 후 성공한 결과여야 합니다.")
        // executeGet()가 세 번 호출되었음을 확인
        verify(newsService, times(3)).executeGet(anyString(), anyMap())
    }

    @Test
    fun `리커버 로직 테스트 - 모든 호출 실패 시 recover 메시지 반환`() {
        // 시뮬레이션:
        // 모든 호출에서 예외를 발생시켜 @Recover가 호출되도록 함
        doThrow(RuntimeException("Simulated Failure"))
            .`when`(newsService).executeGet(anyString(), anyMap())

        val result = newsService.searchNews("리커버 테스트", 10, 1, "sim", "json")
        assertNotNull(result)
        // recover 메서드에서 정의한 에러 메시지가 포함되어 있는지 확인
        assertTrue(
            result.contains("뉴스 서비스를 현재 이용할 수 없습니다"),
            "모든 호출 실패 시 recover 메시지가 반환되어야 합니다."
        )
        // 최대 3번 호출되었음을 확인 (실패 후 recover 호출)
        verify(newsService, times(3)).executeGet(anyString(), anyMap())
    }
}
