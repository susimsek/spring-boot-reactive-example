package io.github.susimsek.springbootreactiveexample.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(name = "CreateTodoDTO", description = "DTO for creating a new Todo task")
data class CreateTodoDTO(

    @field:NotBlank
    @field:Size(min = 1, max = 255)
    @field:Pattern(
        regexp = "^[a-zA-Z0-9ğüşöçıİĞÜŞÖÇ,.!? ]+$",
        message = "Title contains invalid characters"
    )
    @Schema(
        description = "Title of the Todo task",
        example = "Buy groceries",
        required = true,
        minLength = 1,
        maxLength = 255
    )
    val title: String
)
