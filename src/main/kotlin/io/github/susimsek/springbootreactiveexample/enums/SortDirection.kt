package io.github.susimsek.springbootreactiveexample.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    enumAsRef = true,
    description = "Sorting direction:\n" +
        "* `ASC` - Ascending order\n" +
        "* `DESC` - Descending order\n"
)
enum class SortDirection {
    ASC,
    DESC
}
