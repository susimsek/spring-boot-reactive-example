package io.github.susimsek.springbootreactiveexample.config.logging.enums

/**
 * Represents the type of HTTP log entry.
 *
 * This enum is used to differentiate between logging for HTTP requests and responses.
 */
enum class HttpLogType {

    /**
     * Represents an HTTP request log.
     *
     * This type is used when logging details about an outgoing or incoming HTTP request.
     */
    REQUEST,

    /**
     * Represents an HTTP response log.
     *
     * This type is used when logging details about an HTTP response received from a server.
     */
    RESPONSE
}
