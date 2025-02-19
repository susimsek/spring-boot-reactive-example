package io.github.susimsek.springbootreactiveexample.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(
    name = "UpdateTodoDTO",
    description = "Request DTO for fully updating an existing Todo task"
)
data class UpdateTodoDTO(

    @field:NotBlank
    @field:Size(min = 1, max = 255)
    @field:Pattern(
        regexp = "^[a-zA-Z0-9ğüşöçıİĞÜŞÖÇ,.!? ]+$",
        message = "Title contains invalid characters"
    )
    @Schema(
        description = "Updated title of the Todo task",
        example = "Go for a walk",
        required = true,
        minLength = 1,
        maxLength = 255
    )
    val title: String,

    @field:NotNull
    @Schema(
        description = "Updated status of the Todo task indicating whether it is completed",
        example = "true",
        required = true
    )
    val completed: Boolean
)
