package com.coing.domain.news.service

import com.coing.domain.coin.market.repository.MarketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.context.annotation.Lazy
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

@Service
open class NewsService(
	private val marketRepository: MarketRepository
) {

	@Value("\${naver.client.id}")
	private lateinit var clientId: String

	@Value("\${naver.client.secret}")
	private lateinit var clientSecret: String

	// 자기 자신을 @Lazy로 주입받아 프록시를 통한 호출을 보장합니다.
	@Autowired
	@Lazy
	private lateinit var self: NewsService

	/**
	 * 마켓 코드에 해당하는 마켓의 한국 이름을 검색어로 뉴스 조회.
	 * 내부에서 self를 통해 @Retryable이 적용된 searchNews 메서드를 호출합니다.
	 */
	open fun searchNewsByMarketCode(
		marketCode: String,
		display: Int,
		start: Int,
		sort: String,
		format: String
	): String {
		val market = marketRepository.findByCode(marketCode)
			?: throw NoSuchElementException("Market not found for code: $marketCode")
		val query = market.koreanName
		// 자기 자신(self)을 통해 호출함으로써 프록시를 경유하게 됩니다.
		return self.searchNews(query, display, start, sort, format)
	}

	/**
	 * 뉴스 검색 API 호출 (리트라이 대상)
	 */
	@Retryable(
		value = [RuntimeException::class],
		maxAttempts = 3,
		backoff = Backoff(delay = 2000)
	)
	open fun searchNews(
		query: String,
		display: Int,
		start: Int,
		sort: String,
		format: String
	): String {
		val encodedQuery = try {
			URLEncoder.encode(query, "UTF-8")
		} catch (e: Exception) {
			throw RuntimeException("검색어 인코딩 실패", e)
		}
		// 실제 서비스에서는 URL이 올바르게 구성되어야 하지만, 오류 검증을 위해 일부러 잘못된 URL을 넣어 재시도 흐름을 테스트할 수 있습니다.
		val apiURL = "https://openapi.naver.com/v1/search/news.$format" +
				"?query=$encodedQuery&display=$display&start=$start&sort=$sort"
		val requestHeaders = mapOf(
			"X-Naver-Client-Id" to clientId,
			"X-Naver-Client-Secret" to clientSecret
		)
		return executeGet(apiURL, requestHeaders)
	}

	/**
	 * HTTP GET 요청을 실행합니다.
	 * HTTP_OK(200)가 아닌 응답이면 예외를 발생시켜 리트라이 로직을 트리거합니다.
	 */
	open fun executeGet(apiUrl: String, requestHeaders: Map<String, String>): String {
		val connection = connect(apiUrl)
		return try {
			connection.requestMethod = "GET"
			requestHeaders.forEach { (key, value) ->
				connection.setRequestProperty(key, value)
			}
			// HTTP 상태 코드가 200이 아니면 예외 발생
			if (connection.responseCode != HttpURLConnection.HTTP_OK) {
				throw RuntimeException("HTTP error! Status: ${connection.responseCode}")
			}
			val stream = connection.inputStream
			readBody(stream)
		} catch (e: Exception) {
			throw RuntimeException("API 요청과 응답 실패", e)
		} finally {
			connection.disconnect()
		}
	}

	/**
	 *  최대 시도 후에도 실패 시 @Recover 메서드가 호출되어 지정된 에러 메시지를 반환합니다.
	 */
	@Recover
	open fun recover(
		e: RuntimeException,
		query: String,
		display: Int,
		start: Int,
		sort: String,
		format: String
	): String {
		return "{\"error\": \"뉴스 서비스를 현재 이용할 수 없습니다. 잠시 후 다시 시도해 주세요.\"}"
	}

	private fun connect(apiUrl: String): HttpURLConnection {
		return try {
			val url = URI.create(apiUrl).toURL()
			url.openConnection() as HttpURLConnection
		} catch (e: Exception) {
			throw RuntimeException("연결 실패: $apiUrl", e)
		}
	}

	private fun readBody(body: InputStream): String {
		InputStreamReader(body).use { streamReader ->
			BufferedReader(streamReader).use { lineReader ->
				return buildString {
					var line: String?
					while (lineReader.readLine().also { line = it } != null) {
						append(line)
					}
				}
			}
		}
	}
}
