package io.github.susimsek.springbootreactiveexample.config.logging.enums

/**
 * Defines different levels of HTTP logging for requests and responses.
 *
 * This enum is used to control the amount of information logged when handling HTTP communication.
 * It helps in debugging, monitoring, and analyzing API interactions efficiently.
 */
enum class HttpLogLevel {

    /**
     * Disables logging completely.
     *
     * No HTTP request or response details will be logged.
     */
    NONE,

    /**
     * Logs only basic request and response details.
     *
     * Typically includes HTTP method, URL, and response status code.
     * Does not log headers or body content.
     */
    BASIC,

    /**
     * Logs request and response headers in addition to basic details.
     *
     * Useful for debugging API requests without exposing sensitive body content.
     */
    HEADERS,

    /**
     * Logs the full request and response details including headers and body content.
     *
     * This mode is useful for deep debugging but should be used with caution
     * to avoid logging sensitive data or overwhelming log files.
     */
    FULL
}
