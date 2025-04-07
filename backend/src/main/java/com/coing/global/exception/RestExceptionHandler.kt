package com.coing.global.exception

import com.coing.util.BasicResponse
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.slf4j.LoggerFactory

@RestControllerAdvice
class RestExceptionHandler(
	private val messageSource: MessageSource
) {

	private val log = LoggerFactory.getLogger(RestExceptionHandler::class.java)

	@ExceptionHandler(BusinessException::class)
	fun handleBusinessException(exception: BusinessException): ResponseEntity<BasicResponse> {
		log.warn("[ExceptionHandler] Message: {}, Detail: {}", exception.message, exception.detail)
		return if (exception.message?.contains("인증 토큰") == true) {
			ResponseEntity.status(HttpStatus.FORBIDDEN).body(BasicResponse.of(exception))
		} else {
			BasicResponse.to(exception)
		}
	}

	@ExceptionHandler(ExpiredJwtException::class)
	fun handleExpiredJwtException(ex: ExpiredJwtException): ResponseEntity<BasicResponse> {
		log.warn("[ExceptionHandler] ExpiredJwtException: {}", ex.message)
		val businessException = BusinessException("토큰 만료", HttpStatus.FORBIDDEN, ex.message ?: "")
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BasicResponse.of(businessException))
	}

	@ExceptionHandler(RuntimeException::class)
	fun handleRuntimeException(exception: RuntimeException): ResponseEntity<BasicResponse> {
		log.error("[ExceptionHandler] Runtime exception occurred: ", exception)

		val businessException = BusinessException("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, exception.message ?: "")
		log.error("[ExceptionHandler] Message: {}, Detail: {}", businessException.message, businessException.detail)

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(BasicResponse.of(businessException))
	}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<BasicResponse> {
		val errors = ex.bindingResult.fieldErrors
			.joinToString(", ") { error ->
				messageSource.getMessage(error, LocaleContextHolder.getLocale())
			}

		val businessException = BusinessException("Validation Error", HttpStatus.BAD_REQUEST, errors)
		log.warn("[ExceptionHandler] Validation errors: {}", errors)
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BasicResponse.of(businessException))
	}
}
