package com.coing.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

	@Bean
	fun objectMapper(): ObjectMapper {
		return ObjectMapper()
			.registerModule(KotlinModule.Builder().build())
			.registerModule(JavaTimeModule()) // Java 8 날짜/시간 지원 추가
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // timestamp 변환 방지
	}
}
