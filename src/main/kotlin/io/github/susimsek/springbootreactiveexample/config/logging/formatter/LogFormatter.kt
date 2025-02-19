package io.github.susimsek.springbootreactiveexample.config.logging.formatter

import io.github.susimsek.springbootreactiveexample.config.logging.model.HttpLog

/**
 * Defines a contract for formatting HTTP log entries.
 *
 * Implementations of this interface should provide a structured way to format
 * [HttpLog] instances into loggable string representations.
 *
 * Example usage:
 * ```
 * class JsonLogFormatter : LogFormatter {
 *     override fun format(httpLog: HttpLog): String {
 *         return ObjectMapper().writeValueAsString(httpLog)
 *     }
 * }
 * ```
 */
interface LogFormatter {

    /**
     * Formats an [HttpLog] instance into a string representation.
     *
     * @param httpLog The HTTP log entry to be formatted.
     * @return A formatted string representing the log entry.
     */
    fun format(httpLog: HttpLog): String
}
