package com.coing.global.init

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Profile("dev")
@Configuration
class DevInitData {

	private val log = LoggerFactory.getLogger(DevInitData::class.java)

	@Bean
	fun devApplicationRunner(): ApplicationRunner {
		return ApplicationRunner {
			genApiJsonFile("http://localhost:8080/v3/api-docs/api", "../api_schema.json")
		}
	}

	fun genApiJsonFile(url: String, filename: String) {
		val filePath = Path.of(filename)
		val client = HttpClient.newHttpClient()
		val request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.GET()
			.build()

		try {
			val response = client.send(request, HttpResponse.BodyHandlers.ofString())

			if (response.statusCode() == 200) {
				Files.writeString(
					filePath,
					response.body(),
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING
				)
			} else {
				log.error("Error writing Api file {}", response.statusCode())
			}
		} catch (e: IOException) {
			log.error("Request failed {}", e.message)
		} catch (e: InterruptedException) {
			log.error("Request failed {}", e.message)
			Thread.currentThread().interrupt() // 중요: 인터럽트 플래그 복구
		}
	}
}
