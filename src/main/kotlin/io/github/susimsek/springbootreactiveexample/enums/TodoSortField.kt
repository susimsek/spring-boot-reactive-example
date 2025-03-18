package io.github.susimsek.springbootreactiveexample.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    enumAsRef = true,
    description = "Sorting field:\n" +
        "* `CREATED_AT` - Sort by creation date\n" +
        "* `UPDATED_AT` - Sort by update date\n" +
        "* `TITLE` - Sort by title\n" +
        "* `COMPLETED` - Sort by completion status\n"
)
enum class TodoSortField {
    CREATED_AT,
    UPDATED_AT,
    TITLE,
    COMPLETED
}
