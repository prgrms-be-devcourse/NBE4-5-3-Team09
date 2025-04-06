package com.coing.domain.user.email.service

import com.coing.domain.user.entity.User
import com.coing.domain.user.repository.UserRepository
import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import com.coing.util.Ut
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val emailSenderService: EmailSenderService,
    private val messageUtil: MessageUtil
) {

    @Value("\${custom.jwt.secret-key}")
    lateinit var jwtSecretKey: String

    private val log = LoggerFactory.getLogger(EmailVerificationService::class.java)

    /**
     * 회원가입 후, 이메일 인증 토큰을 생성하여 이메일 전송하고 DB 업데이트
     */
    @Async
    @Transactional
    fun sendVerificationEmail(user: User) {
        // 이메일 인증 토큰 생성 (JWT 기반, 만료 10분)
        val token = Ut.AuthTokenUtil.createEmailVerificationToken(jwtSecretKey, user.id)
        try {
            emailSenderService.sendEmailVerificationMessage(user.email, token)
            log.info("인증 이메일 전송 성공: {}", user.email)
        } catch (e: Exception) {
            log.error("인증 이메일 전송 에러: {}", user.email, e)
            throw BusinessException(
                messageUtil.resolveMessage("mail.send.fail"),
                HttpStatus.BAD_REQUEST,
                ""
            )
        }
    }

    /**
     * 사용자가 인증 링크를 클릭하면 호출되는 엔드포인트에서 토큰을 검증하고, 이메일 인증 상태를 업데이트
     */
    @Transactional
    fun verifyEmail(userId: UUID): User {
        val user = userRepository.findById(userId).orElseThrow {
            BusinessException(messageUtil.resolveMessage("user.not.found"), HttpStatus.BAD_REQUEST, "")
        }
        // 불변 엔티티 업데이트 (setter 대신 with 메서드 또는 커스텀 변경 메서드 사용)
        val verifiedUser = user.verifyEmail()
        return userRepository.save(verifiedUser)
    }

    // 이메일 재전송
    @Async
    @Transactional
    fun resendVerificationEmail(userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow {
            BusinessException(messageUtil.resolveMessage("user.not.found"), HttpStatus.BAD_REQUEST, "")
        }
        if (user.verified) {
            throw BusinessException("이미 인증된 사용자입니다.", HttpStatus.BAD_REQUEST, "")
        }
        sendVerificationEmail(user)
    }
}
