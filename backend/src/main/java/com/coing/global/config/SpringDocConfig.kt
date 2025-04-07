package com.coing.global.config

import com.coing.global.exception.doc.ApiErrorCodeExamples
import com.coing.global.exception.doc.ErrorCode
import com.coing.global.exception.doc.ExampleHolder
import com.coing.util.BasicResponse
import com.coing.util.MessageUtil
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Slf4j
@RequiredArgsConstructor
@Configuration
@OpenAPIDefinition(info = Info(title = "Coing API", version = "v0"))
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT"
)
class SpringDocConfig(
	private val messageUtil: MessageUtil
) {

	@Bean
	fun publicApi(): GroupedOpenApi {
		return GroupedOpenApi.builder()
			.group("api")
			.pathsToMatch("/api/**")
			.addOperationCustomizer(customize())
			.build()
	}

	@Bean
	fun customize(): OperationCustomizer {
		return OperationCustomizer { operation, handlerMethod ->
			val apiErrorCodeExamples = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples::class.java)
			if (apiErrorCodeExamples != null) {
				addErrorExamplesToResponses(operation.responses, apiErrorCodeExamples.value)
			}
			operation
		}
	}

	private fun addErrorExamplesToResponses(responses: ApiResponses, errorCodes: Array<out ErrorCode>) {
		val groupedExamples = errorCodes.map { createExampleHolder(it) }
			.groupBy { it.statusCode }

		groupedExamples.forEach { (status, exampleHolders) ->
			val content = Content()
			val mediaType = MediaType()
			val apiResponse = ApiResponse()

			exampleHolders.forEach { holder ->
				mediaType.addExamples(holder.name, holder.example)
			}

			content.addMediaType("application/json", mediaType)
			apiResponse.content = content
			responses.addApiResponse(status.toString(), apiResponse)
		}
	}

	private fun createExampleHolder(errorCode: ErrorCode): ExampleHolder {
		val example = Example()
		example.value = BasicResponse.from(errorCode, messageUtil)

		return ExampleHolder(
			example = example,
			statusCode = errorCode.status.value(),
			name = errorCode.name)
	}
}
