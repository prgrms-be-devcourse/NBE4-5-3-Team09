package com.coing.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
class MailConfig {

	@Value("\${spring.mail.username}")
	private lateinit var mailUserName: String

	@Value("\${spring.mail.password}")
	private lateinit var mailPassword: String

	@Bean
	fun javaMailSender(): JavaMailSender {
		val mailSender = JavaMailSenderImpl()
		mailSender.host = "smtp.gmail.com"
		mailSender.port = 587
		mailSender.username = mailUserName
		mailSender.password = mailPassword

		val props: Properties = mailSender.javaMailProperties
		props["mail.transport.protocol"] = "smtp"
		props["mail.smtp.auth"] = "true"
		props["mail.smtp.starttls.enable"] = "true"
		props["mail.smtp.ssl.protocols"] = "TLSv1.2"
		props["mail.debug"] = "true"

		return mailSender
	}
}
