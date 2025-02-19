package io.github.susimsek.springbootreactiveexample.config.logging.filter

import io.github.susimsek.springbootreactiveexample.config.logging.enums.HttpLogLevel
import io.github.susimsek.springbootreactiveexample.config.logging.enums.HttpLogType
import io.github.susimsek.springbootreactiveexample.config.logging.enums.Source
import io.github.susimsek.springbootreactiveexample.config.logging.formatter.LogFormatter
import io.github.susimsek.springbootreactiveexample.config.logging.model.HttpLog
import io.github.susimsek.springbootreactiveexample.config.logging.utils.DataBufferCopyUtils
import io.github.susimsek.springbootreactiveexample.config.logging.utils.Obfuscator
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.util.AntPathMatcher
import org.springframework.util.StopWatch
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.charset.StandardCharsets

/**
 * A **Spring WebFlux filter** for logging incoming HTTP requests and outgoing responses.
 *
 * This filter logs HTTP metadata such as method, URL, headers, and optionally body content.
 * It supports:
 * - **Obfuscation** of sensitive headers and body fields.
 * - **Exclusion rules** for paths and HTTP methods.
 * - **Performance measurement** via StopWatch.
 * - **Asynchronous, non-blocking execution** in a WebFlux environment.
 *
 * @property logFormatter Formats logs before they are written to the logger.
 * @property obfuscator Utility for masking sensitive data in headers and body.
 * @property httpLogLevel Determines how much information is logged.
 * @property sensitiveHeaders Headers that should be masked in logs.
 * @property shouldNotLogPatterns URL patterns that should not be logged.
 * @property sensitiveJsonBodyFields JSON fields that should be masked in body logs.
 * @property sensitiveParameters Query parameters that should be masked.
 */
class LoggingFilter private constructor(
    private val logFormatter: LogFormatter,
    private val obfuscator: Obfuscator,
    private val httpLogLevel: HttpLogLevel,
    private val sensitiveHeaders: List<String>,
    private val shouldNotLogPatterns: List<Pair<HttpMethod?, String>>,
    private val sensitiveJsonBodyFields: List<String>,
    private val sensitiveParameters: List<String>
) : WebFilter {

    private val logger = LoggerFactory.getLogger(LoggingFilter::class.java)
    private val pathMatcher = AntPathMatcher()

    /**
     * Intercepts HTTP requests and applies logging rules.
     *
     * @param exchange The HTTP exchange containing request and response.
     * @param chain The WebFilterChain for processing the request.
     * @return A `Mono<Void>` indicating the filter execution status.
     */
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (httpLogLevel == HttpLogLevel.NONE || shouldNotLog(exchange.request)) {
            return chain.filter(exchange)
        }
        return mono { processRequest(exchange, chain) }.then()
    }

    /**
     * Processes and logs the HTTP request and response asynchronously.
     *
     * @param exchange The current HTTP exchange.
     * @param chain The WebFilterChain.
     */
    private suspend fun processRequest(exchange: ServerWebExchange, chain: WebFilterChain) {
        val stopWatch = StopWatch().apply { start() }
        val uri = maskUri(exchange.request)
        val request = exchange.request
        val decoratedRequest = object : ServerHttpRequestDecorator(exchange.request) {

            private var capturedRequestBody: String = ""

            init {
                val contentLength = delegate.headers.getFirst(HttpHeaders.CONTENT_LENGTH)?.toLongOrNull() ?: 0L
                if (contentLength == 0L) {
                    logRequest(delegate, uri, capturedRequestBody)
                }
            }

            override fun getBody(): Flux<DataBuffer> {
                return if (httpLogLevel == HttpLogLevel.FULL) {
                    Flux.from(
                        io.github.susimsek.springbootreactiveexample.config.logging.utils.DataBufferCopyUtils.wrapAndBuffer(super.getBody()) { bytes ->
                            capturedRequestBody = String(bytes, StandardCharsets.UTF_8)
                        }
                    ).doOnComplete {
                        logRequest(this, uri, capturedRequestBody)
                    }
                } else {
                    logRequest(this, uri, capturedRequestBody)
                    super.getBody()
                }
            }
        }

        val originalResponse = exchange.response
        val decoratedResponse = object : ServerHttpResponseDecorator(originalResponse) {
            private var capturedResponseBody: String = ""

            init {
                beforeCommit {
                    stopWatch.stop()
                    val durationMs = stopWatch.totalTimeMillis
                    logResponse(delegate, uri, request.method, capturedResponseBody, durationMs)
                    Mono.empty()
                }
            }

            override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
                if (httpLogLevel == HttpLogLevel.FULL) {
                    val wrappedBody = io.github.susimsek.springbootreactiveexample.config.logging.utils.DataBufferCopyUtils.wrapAndBuffer(body) { bytes ->
                        capturedResponseBody = String(bytes, StandardCharsets.UTF_8)
                    }
                    return super.writeWith(wrappedBody)
                }
                return super.writeWith(body)
            }
        }

        val mutatedExchange = exchange.mutate()
            .request(decoratedRequest)
            .response(decoratedResponse)
            .build()

        chain.filter(mutatedExchange).awaitSingleOrNull()
    }

    /**
     * Logs HTTP request details, including headers and body.
     *
     * The request body and headers are masked if they contain sensitive data.
     *
     * @param request The HTTP request to be logged.
     * @param uri The request URI.
     * @param body The request body (masked if sensitive fields are defined).
     */
    private fun logRequest(
        request: ServerHttpRequest,
        uri: URI,
        body: String
    ) {
        val maskedBody = if (sensitiveJsonBodyFields.isNotEmpty()) {
            obfuscator
                .maskJsonBody(body, sensitiveJsonBodyFields)
        } else {
            body
        }
        val obfuscatedHeaders: HttpHeaders? = if (isHttpLogLevel(HttpLogLevel.HEADERS)) {
            obfuscator.obfuscateHeaders(request.headers, sensitiveHeaders)
        } else {
            null
        }
        val httpLog = HttpLog(
            type = HttpLogType.REQUEST,
            method = request.method,
            uri = uri,
            statusCode = null,
            headers = obfuscatedHeaders,
            body = maskedBody,
            source = Source.SERVER,
            durationMs = null
        )
        logger.info("HTTP Request: {}", logFormatter.format(httpLog))
    }

    /**
     * Logs HTTP response details, including headers, body, and execution time.
     *
     * The response body and headers are masked if they contain sensitive data.
     *
     * @param response The HTTP response to be logged.
     * @param uri The request URI associated with the response.
     * @param method The HTTP method of the request.
     * @param body The response body (masked if sensitive fields are defined).
     * @param durationMs The time taken to process the request in milliseconds.
     */
    private fun logResponse(
        response: ServerHttpResponse,
        uri: URI,
        method: HttpMethod,
        body: String,
        durationMs: Long
    ) {
        val maskedBody = if (sensitiveJsonBodyFields.isNotEmpty()) {
            obfuscator
                .maskJsonBody(body, sensitiveJsonBodyFields)
        } else {
            body
        }
        val obfuscatedHeaders: HttpHeaders? = if (isHttpLogLevel(HttpLogLevel.HEADERS)) {
            obfuscator.obfuscateHeaders(response.headers, sensitiveHeaders)
        } else {
            null
        }
        val httpLog = HttpLog(
            type = HttpLogType.RESPONSE,
            method = method,
            uri = uri,
            statusCode = response.statusCode?.value(),
            headers = obfuscatedHeaders,
            body = maskedBody,
            source = Source.SERVER,
            durationMs = durationMs
        )
        logger.info("HTTP Response: {}", logFormatter.format(httpLog))
    }

    /**
     * Determines whether the given request should be excluded from logging.
     *
     * Checks if the request method and path match any of the predefined patterns
     * in `shouldNotLogPatterns`, allowing certain requests to be skipped from logging.
     *
     * @param request The incoming HTTP request.
     * @return `true` if the request matches an exclusion pattern, otherwise `false`.
     */
    private fun shouldNotLog(request: ServerHttpRequest): Boolean {
        val requestPath = request.uri.path
        val requestMethod = request.method
        return shouldNotLogPatterns.any { (method, pattern) ->
            (method == null || method == requestMethod) && pathMatcher.match(pattern, requestPath)
        }
    }

    /**
     * Checks if the current logging level is equal to or more detailed than the specified level.
     *
     * @param level The log level to compare against.
     * @return `true` if `httpLogLevel` is greater than or equal to `level`, otherwise `false`.
     */
    private fun isHttpLogLevel(level: HttpLogLevel): Boolean {
        return httpLogLevel.ordinal >= level.ordinal
    }

    /**
     * Masks sensitive query parameters in the request URI to protect sensitive data.
     *
     * @param request The incoming HTTP request.
     * @return The masked URI if sensitive parameters are present, otherwise the original URI.
     */
    private fun maskUri(request: ServerHttpRequest): URI {
        val maskedUri = if (sensitiveParameters.isNotEmpty()) {
            obfuscator.maskParameters(request.uri, sensitiveParameters)
        } else {
            request.uri
        }
        return maskedUri
    }

    /**
     * Companion object providing a static method to create a new [Builder] instance.
     *
     * This method allows for an easy and fluent way to configure and instantiate a [LoggingFilter].
     * It initializes the builder with required dependencies: [logFormatter] for formatting logs
     * and [obfuscator] for masking sensitive data.
     *
     * Example usage:
     * ```
     * val loggingFilter = LoggingFilter.builder(logFormatter, obfuscator)
     *     .httpLogLevel(HttpLogLevel.BASIC)
     *     .sensitiveHeader("Authorization", "X-Secret-Key")
     *     .shouldNotLog(HttpMethod.GET, "/health", "/metrics")
     *     .sensitiveJsonBodyField("password", "ssn")
     *     .build()
     * ```
     *
     * @param logFormatter The formatter used for structuring logs.
     * @param obfuscator The utility responsible for masking sensitive information.
     * @return A new instance of [Builder] for configuring [LoggingFilter].
     */
    companion object {
        fun builder(
            logFormatter: LogFormatter,
            obfuscator: Obfuscator
        ): Builder {
            return Builder(logFormatter, obfuscator)
        }
    }

    /**
     * A builder class for configuring and creating an instance of [LoggingFilter].
     *
     * This builder provides methods to customize logging behavior by setting log levels,
     * defining sensitive headers and JSON body fields, and specifying which paths or parameters
     * should be masked or excluded from logging.
     *
     * Example usage:
     * ```
     * val loggingFilter = LoggingFilter.builder(logFormatter, obfuscator)
     *     .httpLogLevel(HttpLogLevel.HEADERS)
     *     .sensitiveHeader("Authorization", "X-Api-Key")
     *     .shouldNotLog(HttpMethod.GET, "/health", "/metrics")
     *     .sensitiveJsonBodyField("password", "ssn")
     *     .build()
     * ```
     *
     * @property logFormatter Formats the log output.
     * @property obfuscator Utility for masking sensitive data in headers and request bodies.
     */
    class Builder(
        private val logFormatter: LogFormatter,
        private val obfuscator: Obfuscator
    ) {
        /** The log level defining how much detail should be logged. Default is `FULL`. */
        private var httpLogLevel: HttpLogLevel = HttpLogLevel.FULL

        /** Headers that should be masked in logs to protect sensitive information. */
        private val sensitiveHeaders: MutableList<String> =
            mutableListOf("Authorization", "Cookie", "Set-Cookie")

        /** URL patterns that should not be logged to avoid excessive or unnecessary logging. */
        private val shouldNotLogPatterns: MutableList<Pair<HttpMethod?, String>> = mutableListOf()

        /** JSON body fields that should be masked in request and response logs. */
        private val sensitiveJsonBodyFields: MutableList<String> = mutableListOf(
            "access_token",
            "refresh_token"
        )

        /** Query parameters that should be masked in the logged request URLs. */
        private val sensitiveParameters: MutableList<String> = mutableListOf(
            "access_token"
        )

        /**
         * Sets the log level for HTTP logging.
         *
         * @param level The desired [HttpLogLevel] (e.g., `BASIC`, `HEADERS`, `FULL`).
         * @return The updated builder instance.
         */
        fun httpLogLevel(level: HttpLogLevel) = apply { this.httpLogLevel = level }

        /**
         * Adds headers to the list of sensitive headers that should be masked in logs.
         *
         * @param headers The names of headers to be masked.
         * @return The updated builder instance.
         */
        fun sensitiveHeader(vararg headers: String) = apply { this.sensitiveHeaders.addAll(headers) }

        /**
         * Specifies patterns for requests that should not be logged.
         *
         * @param method The HTTP method to match (optional, if null, applies to all methods).
         * @param patterns URL patterns (e.g., `/health`, `/metrics`) to be excluded from logging.
         * @return The updated builder instance.
         */
        fun shouldNotLog(method: HttpMethod?, vararg patterns: String) = apply {
            patterns.forEach { this.shouldNotLogPatterns.add(Pair(method, it)) }
        }

        /**
         * Specifies URL patterns that should not be logged, regardless of HTTP method.
         *
         * @param patterns URL patterns to exclude from logging.
         * @return The updated builder instance.
         */
        fun shouldNotLog(vararg patterns: String) = apply {
            this.shouldNotLogPatterns.addAll(patterns.map { Pair(null, it) })
        }

        /**
         * Adds JSON fields that should be masked when logging request or response bodies.
         *
         * @param paths The names of JSON fields to be masked.
         * @return The updated builder instance.
         */
        fun sensitiveJsonBodyField(vararg paths: String) = apply {
            this.sensitiveJsonBodyFields.addAll(paths)
        }

        /**
         * Adds query parameters that should be masked when logging request URLs.
         *
         * @param params The names of query parameters to be masked.
         * @return The updated builder instance.
         */
        fun sensitiveParameter(vararg params: String) = apply {
            this.sensitiveParameters.addAll(params)
        }

        /**
         * Builds and returns a configured instance of [LoggingFilter].
         *
         * @return The configured [LoggingFilter] instance.
         */
        fun build(): LoggingFilter {
            return LoggingFilter(
                logFormatter,
                obfuscator,
                httpLogLevel,
                sensitiveHeaders,
                shouldNotLogPatterns,
                sensitiveJsonBodyFields,
                sensitiveParameters
            )
        }
    }
}
