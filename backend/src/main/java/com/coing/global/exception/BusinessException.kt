package com.coing.global.exception

import org.springframework.http.HttpStatus

open class BusinessException : RuntimeException {
	val messageCode: String
	val status: HttpStatus
	val detail: String

	constructor(messageCode: String, status: HttpStatus) : super(messageCode) {
		this.messageCode = messageCode
		this.status = status
		this.detail = ""
	}

	constructor(messageCode: String, status: HttpStatus, detail: String) : super(messageCode) {
		this.messageCode = messageCode
		this.status = status
		this.detail = detail
	}

	constructor(messageCode: String, tokenError: Boolean) : super(messageCode) {
		this.messageCode = messageCode
		this.status = if (tokenError) HttpStatus.FORBIDDEN else HttpStatus.BAD_REQUEST
		this.detail = ""
	}
}
