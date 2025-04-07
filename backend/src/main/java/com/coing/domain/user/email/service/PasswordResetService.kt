package com.coing.domain.user.email.service

import com.coing.domain.user.entity.User
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.coing.util.Ut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasswordResetService(
    private val emailSenderService: EmailSenderService,
    private val messageUtil: MessageUtil
) {

    @Value("\${custom.jwt.secret-key}")
    private lateinit var jwtSecretKey: String

    // 비밀번호 재설정 이메일 전송 (코루틴 기반, IO 디스패처 사용)
    @Transactional
    suspend fun sendPasswordResetEmail(user: User) = withContext(Dispatchers.IO) {
        // JWT 토큰 생성 (만료: 1시간 = 3600초)
        val claims = mapOf("id" to user.id)
        val token = Ut.Jwt.createToken(jwtSecretKey, 3600, claims)
        try {
            emailSenderService.sendPasswordResetEmailMessage(user.email, token)
            logger.info("비밀번호 재설정 이메일 전송 성공: {}", user.email)
        } catch (e: Exception) {
            logger.error("비밀번호 재설정 이메일 전송 에러: {}", user.email, e)
            throw BusinessException(messageUtil.resolveMessage("mail.send.fail"), HttpStatus.BAD_REQUEST, "")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PasswordResetService::class.java)
    }
}