package com.coing.domain.user.email.service

import com.coing.global.exception.BusinessException
import com.coing.util.MessageUtil
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailSenderService(
    private val javaMailSender: JavaMailSender,
    private val messageUtil: MessageUtil
) {

    @Value("\${spring.mail.username}")
    private lateinit var senderEmail: String

    @Value("\${custom.jwt.mail-verification-url}")
    private lateinit var emailVerificationUrl: String

    @Value("\${custom.jwt.password-reset-url}")
    private lateinit var passwordResetUrl: String

    @Throws(MessagingException::class)
    fun createEmailVerificationMail(recipientEmail: String, token: String): MimeMessage {
        val message = javaMailSender.createMimeMessage()
        message.setFrom(senderEmail)
        message.setRecipients(MimeMessage.RecipientType.TO, recipientEmail)
        message.setSubject("이메일 인증")
        // 인증 링크 구성 (추후 실제 url로 환경 변수 설정 필요)
        val verificationLink = "$emailVerificationUrl$token"
        val body = """
            <h3>아래 링크를 클릭하여 이메일 인증을 완료하세요.</h3>
            <p><a href="$verificationLink">이메일 인증하기</a></p>
        """.trimIndent()
        message.setText(body, "UTF-8", "html")
        return message
    }

    @Throws(MessagingException::class)
    fun sendEmailVerificationMessage(recipientEmail: String, token: String) {
        val message = createEmailVerificationMail(recipientEmail, token)
        try {
            javaMailSender.send(message)
            logger.info("인증 이메일 전송 성공: {}", recipientEmail)
        } catch (e: MailException) {
            logger.error("인증 이메일 전송 에러: {}", recipientEmail, e)
            throw BusinessException(messageUtil.resolveMessage("mail.send.fail"), HttpStatus.BAD_REQUEST, "")
        }
    }

    @Throws(MessagingException::class)
    fun createPasswordResetMail(recipientEmail: String, token: String): MimeMessage {
        val message = javaMailSender.createMimeMessage()
        message.setFrom(senderEmail)
        message.setRecipients(MimeMessage.RecipientType.TO, recipientEmail)
        message.setSubject("비밀번호 재설정")
        val resetLink = "$passwordResetUrl$token"
        val body = """
            <h3>아래 링크를 클릭하여 비밀번호 재설정을 진행하세요.</h3>
            <p><a href="$resetLink">비밀번호 재설정하기</a></p>
        """.trimIndent()
        message.setText(body, "UTF-8", "html")
        return message
    }

    @Throws(MessagingException::class)
    fun sendPasswordResetEmailMessage(recipientEmail: String, token: String) {
        val message = createPasswordResetMail(recipientEmail, token)
        try {
            javaMailSender.send(message)
            logger.info("비밀번호 재설정 이메일 전송 성공: {}", recipientEmail)
        } catch (e: MailException) {
            logger.error("비밀번호 재설정 이메일 전송 에러: {}", recipientEmail, e)
            throw BusinessException(messageUtil.resolveMessage("mail.send.fail"), HttpStatus.BAD_REQUEST, "")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EmailSenderService::class.java)
    }
}
