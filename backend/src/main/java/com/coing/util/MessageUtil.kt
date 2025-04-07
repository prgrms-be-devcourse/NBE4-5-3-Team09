package com.coing.util

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class MessageUtil(
	private val messageSource: MessageSource
) {
	fun resolveMessage(code: String): String {
		return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale())!!
	}
}
