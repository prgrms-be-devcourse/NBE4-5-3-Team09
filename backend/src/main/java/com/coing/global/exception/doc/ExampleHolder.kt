package com.coing.global.exception.doc

import io.swagger.v3.oas.models.examples.Example

data class ExampleHolder(
    val example: Example,
    val statusCode: Int,
    val name: String
)
