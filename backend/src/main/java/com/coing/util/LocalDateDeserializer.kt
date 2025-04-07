package com.coing.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LocalDateDeserializer : JsonDeserializer<LocalDate>() {

	companion object {
		private val FORMATTER_1 = DateTimeFormatter.ofPattern("yyyyMMdd")
		private val FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
	}

	@Throws(IOException::class)
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate {
		val dateText = p.text
		return try {
			LocalDate.parse(dateText, FORMATTER_1)
		} catch (e: DateTimeParseException) {
			LocalDate.parse(dateText, FORMATTER_2)
		}
	}
}
