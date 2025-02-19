package io.github.susimsek.springbootreactiveexample.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Data Transfer Object for greeting messages.
 */
@Schema(description = "Data Transfer Object for greeting messages.")
data class GreetingDTO(

    @Schema(description = "The greeting message content.", example = "Hello, GraalVM Native Image!")
    val message: String
)
