package io.github.susimsek.springbootreactiveexample.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(
    name = "TodoDTO",
    description = "Data Transfer Object (DTO) representing a Todo task"
)
data class TodoDTO(

    @Schema(
        description = "Unique identifier of the Todo task",
        example = "1",
        required = true,
        minimum = "1"
    )
    val id: Long,

    @Schema(
        description = "Title of the Todo task",
        example = "Buy groceries",
        required = true,
        minLength = 1,
        maxLength = 255
    )
    val title: String,

    @Schema(
        description = "Indicates whether the Todo task is completed",
        example = "false",
        required = true
    )
    val completed: Boolean,

    @Schema(
        description = "Timestamp when the Todo task was created (ISO 8601 format)",
        example = "2024-02-18T12:00:00Z",
        required = true
    )
    val createdAt: Instant,

    @Schema(
        description = "Timestamp when the Todo task was last updated (ISO 8601 format)",
        example = "2024-02-19T14:30:00Z",
        required = true
    )
    val updatedAt: Instant
)
