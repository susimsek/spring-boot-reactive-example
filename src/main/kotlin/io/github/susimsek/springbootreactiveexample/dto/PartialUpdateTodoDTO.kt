package io.github.susimsek.springbootreactiveexample.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(
    name = "PartialUpdateTodoDTO",
    description = "Request DTO for partially updating an existing Todo task"
)
data class PartialUpdateTodoDTO(

    @field:Size(min = 1, max = 255)
    @field:Pattern(
        regexp = "^[a-zA-Z0-9ğüşöçıİĞÜŞÖÇ,.!? ]+$",
        message = "Title contains invalid characters"
    )
    @Schema(
        description = "Updated title of the Todo task",
        example = "Go for a walk",
        required = false,
        minLength = 1,
        maxLength = 255
    )
    val title: String? = null,

    @Schema(
        description = "Updated status of the Todo task indicating whether it is completed",
        example = "true",
        required = false
    )
    val completed: Boolean? = null
)
