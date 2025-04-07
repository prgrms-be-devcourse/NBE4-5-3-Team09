package com.coing.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient

@Configuration
class WebSocketClientConfig {

	@Bean
	fun webSocketClient(): WebSocketClient {
		return StandardWebSocketClient()
	}
}
