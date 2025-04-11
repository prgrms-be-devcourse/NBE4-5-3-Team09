package com.coing.global.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.util.*

@Configuration
class FirebaseInitializer(
    @Value("\${firebase.credential.base64}")
    private val firebaseCredentialBase64: String
) {
    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isEmpty()) {
            // Base64 디코딩 후 InputStream으로 변환
            val decodedBytes = Base64.getDecoder().decode(firebaseCredentialBase64)
            val credentials = GoogleCredentials.fromStream(ByteArrayInputStream(decodedBytes))

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            FirebaseApp.initializeApp(options)
        }
    }

    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}
