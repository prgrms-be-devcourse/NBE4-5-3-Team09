package com.coing.global.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

	@Bean
	fun cacheManager(): CacheManager {
		val candlesCache = CaffeineCache(
			"candles",
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofHours(24))
				.maximumSize(230_000)
				.build<Any, Any>()
		)

		val marketsCache = CaffeineCache(
			"markets",
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofHours(6))
				.maximumSize(1_000)
				.build<Any, Any>()
		)

		val tokensCache = CaffeineCache(
			"tempTokens",
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(5))
				.maximumSize(10_000)
				.build<Any, Any>()
		)

		return SimpleCacheManager().apply {
			setCaches(listOf(candlesCache, marketsCache, tokensCache))
		}
	}
}
