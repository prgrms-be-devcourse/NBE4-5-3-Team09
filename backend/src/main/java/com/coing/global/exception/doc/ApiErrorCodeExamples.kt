package com.coing.global.exception.doc

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorCodeExamples(
	vararg val value: ErrorCode
)
