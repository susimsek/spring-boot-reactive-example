package io.github.susimsek.springbootreactiveexample.utils

/**
 * Utility object containing helper methods for string manipulation.
 */
object StringUtils {

    /**
     * Converts a camelCase string into snake_case format.
     *
     * - Converts uppercase letters following lowercase letters to an underscore and the lowercase equivalent.
     * - Converts multiple consecutive uppercase letters into a single underscore and their lowercase form.
     *
     * Example:
     * ```
     * "camelCaseString" -> "camel_case_string"
     * "CamelCaseString" -> "camel_case_string"
     * ```
     *
     * @return The string in snake_case format.
     */
    fun String.toSnakeCase(): String {
        return this.replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("([A-Z])([A-Z][a-z])"), "$1_$2")
            .lowercase()
    }
}
