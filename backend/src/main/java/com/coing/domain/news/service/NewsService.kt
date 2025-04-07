package com.coing.domain.news.service

import com.coing.domain.coin.market.repository.MarketRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.NoSuchElementException

@Service
class NewsService(
	private val marketRepository: MarketRepository
) {

	@Value("\${naver.client.id}")
	private lateinit var clientId: String

	@Value("\${naver.client.secret}")
	private lateinit var clientSecret: String

	/**
	 * 마켓 코드를 받아서 해당 마켓의 한국어 이름을 쿼리로 사용해 뉴스 검색 API 호출
	 */
	fun searchNewsByMarketCode(
		marketCode: String,
		display: Int,
		start: Int,
		sort: String,
		format: String
	): String {
		val market = marketRepository.findByCode(marketCode)
			?: throw NoSuchElementException("Market not found for code: $marketCode")

		val query = market.koreanName
		return searchNews(query, display, start, sort, format)
	}

	/**
	 * 네이버 뉴스 검색 API 호출
	 */
	fun searchNews(
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

		val apiURL = "https://openapi.naver.com/v1/search/news.$format" +
				"?query=$encodedQuery&display=$display&start=$start&sort=$sort"

		val requestHeaders = mapOf(
			"X-Naver-Client-Id" to clientId,
			"X-Naver-Client-Secret" to clientSecret
		)

		return get(apiURL, requestHeaders)
	}

	private fun get(apiUrl: String, requestHeaders: Map<String, String>): String {
		val connection = connect(apiUrl)
		return try {
			connection.requestMethod = "GET"
			requestHeaders.forEach { (key, value) ->
				connection.setRequestProperty(key, value)
			}

			val stream = if (connection.responseCode == HttpURLConnection.HTTP_OK) {
				connection.inputStream
			} else {
				connection.errorStream
			}

			readBody(stream)
		} catch (e: Exception) {
			throw RuntimeException("API 요청과 응답 실패", e)
		} finally {
			connection.disconnect()
		}
	}

	private fun connect(apiUrl: String): HttpURLConnection {
		return try {
			val url = URL(apiUrl)
			url.openConnection() as HttpURLConnection
		} catch (e: Exception) {
			throw RuntimeException("연결 실패: $apiUrl", e)
		}
	}

	private fun readBody(body: InputStream): String {
		val streamReader = InputStreamReader(body)
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
