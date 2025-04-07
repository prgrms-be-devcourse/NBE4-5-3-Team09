package com.coing.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LocalTimeDeserializer : JsonDeserializer<LocalTime>() {

	companion object {
		private val FORMATTER_1 = DateTimeFormatter.ofPattern("HHmmss")
		private val FORMATTER_2 = DateTimeFormatter.ofPattern("HH:mm:ss")
	}

	@Throws(IOException::class)
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalTime {
		val timeText = p.text
		return try {
			LocalTime.parse(timeText, FORMATTER_1)
		} catch (e: DateTimeParseException) {
			LocalTime.parse(timeText, FORMATTER_2)
		}
	}
}
