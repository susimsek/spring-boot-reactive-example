package io.github.susimsek.springbootreactiveexample.config.logging.enums

/**
 * Represents the source of an HTTP log entry.
 *
 * This enum is used to distinguish whether the log entry originates from a server-side or client-side HTTP operation.
 */
enum class Source {

    /**
     * Indicates that the log entry is generated from the server-side.
     *
     * This typically applies to logs for incoming HTTP requests and outgoing HTTP responses handled by the server.
     */
    SERVER,

    /**
     * Indicates that the log entry is generated from the client-side.
     *
     * This applies to logs for outgoing HTTP requests made by the client and responses received from a remote server.
     */
    CLIENT
}
