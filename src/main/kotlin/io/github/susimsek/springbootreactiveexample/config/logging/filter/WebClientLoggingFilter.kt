package io.github.susimsek.springbootreactiveexample.config.logging.filter

import io.github.susimsek.springbootreactiveexample.config.logging.enums.HttpLogLevel
import io.github.susimsek.springbootreactiveexample.config.logging.enums.HttpLogType
import io.github.susimsek.springbootreactiveexample.config.logging.enums.Source
import io.github.susimsek.springbootreactiveexample.config.logging.formatter.LogFormatter
import io.github.susimsek.springbootreactiveexample.config.logging.model.HttpLog
import io.github.susimsek.springbootreactiveexample.config.logging.utils.Obfuscator
import io.github.susimsek.springbootreactiveexample.config.logging.wrapper.BufferingClientHttpRequest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.util.AntPathMatcher
import org.springframework.util.StopWatch
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.charset.StandardCharsets

/**
 * A WebClient filter for logging HTTP requests and responses.
 *
 * This filter logs details about outgoing requests and incoming responses while ensuring
 * sensitive data such as authentication tokens, cookies, and personal information are obfuscated.
 *
 * Supports:
 * - **Configurable logging levels** (e.g., `BASIC`, `HEADERS`, `FULL`).
 * - **Sensitive data masking** for headers, JSON fields, and query parameters.
 * - **Exclusion patterns** to avoid logging specific requests.
 * - **Performance tracking** using execution time measurement.
 *
 * @property logFormatter Formats logs before they are recorded.
 * @property obfuscator Utility for masking sensitive data.
 * @property httpLogLevel Determines how much detail is logged.
 * @property sensitiveHeaders Headers that should be masked in logs.
 * @property shouldNotLogPatterns URL patterns that should be excluded from logging.
 * @property sensitiveJsonBodyFields JSON fields that should be obfuscated.
 * @property sensitiveParameters Query parameters that should be masked in logs.
 */
class WebClientLoggingFilter private constructor(
    private val logFormatter: LogFormatter,
    private val obfuscator: Obfuscator,
    private val httpLogLevel: HttpLogLevel,
    private val sensitiveHeaders: List<String>,
    private val shouldNotLogPatterns: List<Pair<HttpMethod?, String>>,
    private val sensitiveJsonBodyFields: List<String>,
    private val sensitiveParameters: List<String>
) : ExchangeFilterFunction {

    private val logger = LoggerFactory.getLogger(WebClientLoggingFilter::class.java)
    private val pathMatcher = AntPathMatcher()

    /**
     * Applies logging filters to outgoing HTTP requests and responses.
     *
     * @param request The outgoing HTTP request.
     * @param next The exchange function for executing the request.
     * @return A `Mono<ClientResponse>` representing the response.
     */
    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        if (httpLogLevel == HttpLogLevel.NONE || shouldNotLog(request)) {
            return next.exchange(request)
        }
        return mono { processRequest(request, next) }
    }

    /**
     * Processes an HTTP request by logging its details and forwarding it for execution.
     *
     * @param request The outgoing request.
     * @param next The exchange function to proceed with the request execution.
     * @return The processed [ClientResponse] after logging.
     */
    private suspend fun processRequest(request: ClientRequest, next: ExchangeFunction): ClientResponse {
        val stopWatch = StopWatch()
        var requestBody: ByteArray? = null
        stopWatch.start()
        val uri = maskUri(request)

        val processedRequest = if (httpLogLevel == HttpLogLevel.FULL) {
            ClientRequest.from(request)
                .body { outputMessage, context ->
                    BufferingClientHttpRequest(outputMessage).let { bufferingRequest ->
                        request.body().insert(bufferingRequest, context)
                            .doOnSuccess { requestBody = bufferingRequest.requestBody }
                    }
                }
                .build()
        } else {
            request
        }

        val response = next.exchange(processedRequest).awaitSingle()
        stopWatch.stop()

        val durationMs = stopWatch.totalTimeMillis
        logRequest(
            request,
            uri,
            requestBody?.toString(StandardCharsets.UTF_8) ?: ""
        )

        return processResponse(response, request, uri, durationMs)
    }

    /**
     * Processes and logs an HTTP response.
     *
     * @param response The HTTP response received.
     * @param request The original request.
     * @param uri The request URI.
     * @param durationMs The time taken to complete the request.
     * @return The processed [ClientResponse] after logging.
     */
    private suspend fun processResponse(
        response: ClientResponse,
        request: ClientRequest,
        uri: URI,
        durationMs: Long
    ): ClientResponse {
        var responseBody: ByteArray? = null

        val mutatedResponse = if (httpLogLevel == HttpLogLevel.FULL) {
            val responseHeaders = response.headers().asHttpHeaders()
            if (responseHeaders.contentLength > 0 || responseHeaders.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
                val body = response.bodyToMono(ByteArray::class.java).awaitSingleOrNull()
                responseBody = body
                response.mutate().body(
                    Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(body ?: ByteArray(0)))
                ).build()
            } else {
                response
            }
        } else {
            response
        }

        logResponse(
            response,
            uri,
            request.method(),
            responseBody?.toString(StandardCharsets.UTF_8) ?: "",
            durationMs
        )
        return mutatedResponse
    }

    /**
     * Logs the details of an outgoing HTTP request.
     *
     * @param request The request being logged.
     * @param uri The request URI.
     * @param body The request body (masked if sensitive).
     */
    private fun logRequest(
        request: ClientRequest,
        uri: URI,
        body: String
    ) {
        val maskedBody = if (sensitiveJsonBodyFields.isNotEmpty()) {
            obfuscator.maskJsonBody(body, sensitiveJsonBodyFields)
        } else {
            body
        }
        val obfuscatedHeaders: HttpHeaders? = if (isHttpLogLevel(HttpLogLevel.HEADERS)) {
            obfuscator.obfuscateHeaders(request.headers(), sensitiveHeaders)
        } else {
            null
        }
        val httpLog = HttpLog(
            type = HttpLogType.REQUEST,
            method = request.method(),
            uri = uri,
            statusCode = null,
            headers = obfuscatedHeaders,
            body = maskedBody,
            source = Source.CLIENT,
            durationMs = null
        )
        logger.info("HTTP Request: {}", logFormatter.format(httpLog))
    }

    /**
     * Logs the details of an incoming HTTP response.
     *
     * @param response The response being logged.
     * @param uri The request URI.
     * @param method The HTTP method of the request.
     * @param body The response body (masked if sensitive).
     * @param durationMs The time taken to complete the request in milliseconds.
     */
    private fun logResponse(
        response: ClientResponse,
        uri: URI,
        method: HttpMethod,
        body: String,
        durationMs: Long
    ) {
        val maskedBody = if (sensitiveJsonBodyFields.isNotEmpty()) {
            obfuscator.maskJsonBody(body, sensitiveJsonBodyFields)
        } else {
            body
        }
        val obfuscatedHeaders: HttpHeaders? = if (isHttpLogLevel(HttpLogLevel.HEADERS)) {
            obfuscator.obfuscateHeaders(response.headers().asHttpHeaders(), sensitiveHeaders)
        } else {
            null
        }
        val httpLog = HttpLog(
            type = HttpLogType.RESPONSE,
            method = method,
            uri = uri,
            statusCode = response.statusCode().value(),
            headers = obfuscatedHeaders,
            body = maskedBody,
            source = Source.CLIENT,
            durationMs = durationMs
        )
        logger.info("HTTP Response: {}", logFormatter.format(httpLog))
    }

    /**
     * Determines if a request should be excluded from logging.
     *
     * @param request The request to evaluate.
     * @return `true` if logging should be skipped, otherwise `false`.
     */
    private fun shouldNotLog(request: ClientRequest): Boolean {
        val requestPath = request.url().path
        val requestMethod = request.method()
        return shouldNotLogPatterns.any { (method, pattern) ->
            (method == null || method == requestMethod) && pathMatcher.match(pattern, requestPath)
        }
    }

    /**
     * Checks if the current logging level is greater than or equal to the given level.
     *
     * @param level The log level to compare against.
     * @return `true` if the current level allows for the given logging level.
     */
    private fun isHttpLogLevel(level: HttpLogLevel): Boolean {
        return httpLogLevel.ordinal >= level.ordinal
    }

    /**
     * Masks sensitive query parameters in the request URI.
     *
     * @param request The request whose URI needs masking.
     * @return The masked URI if sensitive parameters exist, otherwise the original URI.
     */
    private fun maskUri(request: ClientRequest): URI {
        val maskedUri = if (sensitiveParameters.isNotEmpty()) {
            obfuscator.maskParameters(request.url(), sensitiveParameters)
        } else {
            request.url()
        }
        return maskedUri
    }

    /**
     * Companion object providing a static method to create a new [Builder] instance.
     *
     * This method initializes a `Builder` with the required dependencies: [logFormatter] for structured logging
     * and [obfuscator] for masking sensitive data in request/response logs.
     *
     * Example usage:
     * ```
     * val loggingFilter = WebClientLoggingFilter.builder(logFormatter, obfuscator)
     *     .httpLogLevel(HttpLogLevel.HEADERS)
     *     .sensitiveHeader("Authorization", "X-Api-Key")
     *     .shouldNotLog(HttpMethod.GET, "/health", "/metrics")
     *     .sensitiveJsonBodyField("password", "ssn")
     *     .build()
     * ```
     *
     * @param logFormatter The formatter used for structuring logs.
     * @param obfuscator The utility responsible for masking sensitive information.
     * @return A new instance of [Builder] for configuring [WebClientLoggingFilter].
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
     * Builder class for configuring and creating an instance of [WebClientLoggingFilter].
     *
     * This builder provides methods to customize logging behavior, including setting log levels,
     * defining sensitive headers, masking specific JSON body fields, and excluding certain requests from logging.
     *
     * Example usage:
     * ```
     * val loggingFilter = WebClientLoggingFilter.builder(logFormatter, obfuscator)
     *     .httpLogLevel(HttpLogLevel.FULL)
     *     .sensitiveHeader("Authorization", "Set-Cookie")
     *     .shouldNotLog(HttpMethod.POST, "/login")
     *     .sensitiveJsonBodyField("password", "credit_card")
     *     .sensitiveParameter("access_token")
     *     .build()
     * ```
     *
     * @property logFormatter The log formatter instance.
     * @property obfuscator Utility for masking sensitive data.
     */
    class Builder(
        private val logFormatter: LogFormatter,
        private val obfuscator: Obfuscator
    ) {
        /** Log level to define how much detail should be logged. Default is `FULL`. */
        private var httpLogLevel: HttpLogLevel = HttpLogLevel.FULL

        /** Headers that should be masked to protect sensitive information. */
        private val sensitiveHeaders: MutableList<String> = mutableListOf(
            "Authorization",
            "Cookie",
            "Set-Cookie"
        )

        /** URL patterns that should be excluded from logging. */
        private val shouldNotLogPatterns: MutableList<Pair<HttpMethod?, String>> = mutableListOf()

        /** JSON body fields that should be masked in request and response logs. */
        private val sensitiveJsonBodyFields: MutableList<String> =
            mutableListOf("access_token", "refresh_token")

        /** Query parameters that should be masked in the request URI. */
        private val sensitiveParameters: MutableList<String> = mutableListOf(
            "access_token"
        )

        /**
         * Sets the HTTP log level.
         *
         * @param level The desired [HttpLogLevel] (e.g., `BASIC`, `HEADERS`, `FULL`).
         * @return The updated builder instance.
         */
        fun httpLogLevel(level: HttpLogLevel) = apply { this.httpLogLevel = level }

        /**
         * Adds headers to be masked in logs.
         *
         * @param headers The headers to be masked.
         * @return The updated builder instance.
         */
        fun sensitiveHeader(vararg headers: String) = apply { this.sensitiveHeaders.addAll(headers) }

        /**
         * Adds JSON fields that should be masked in request/response logs.
         *
         * @param fields JSON field names to be masked.
         * @return The updated builder instance.
         */
        fun sensitiveJsonBodyField(vararg fields: String) = apply {
            this.sensitiveJsonBodyFields.addAll(fields)
        }

        /**
         * Adds query parameters that should be masked in the logged URI.
         *
         * @param params The parameter names to be masked.
         * @return The updated builder instance.
         */
        fun sensitiveParameter(vararg params: String) = apply {
            this.sensitiveParameters.addAll(params)
        }

        /**
         * Excludes specific HTTP methods and URL patterns from logging.
         *
         * @param method The HTTP method to filter (optional, applies to all if `null`).
         * @param patterns URL patterns to be excluded from logging.
         * @return The updated builder instance.
         */
        fun shouldNotLog(method: HttpMethod?, vararg patterns: String) = apply {
            patterns.forEach { this.shouldNotLogPatterns.add(Pair(method, it)) }
        }

        /**
         * Excludes specific URL patterns from logging, regardless of HTTP method.
         *
         * @param patterns URL patterns to exclude.
         * @return The updated builder instance.
         */
        fun shouldNotLog(vararg patterns: String) = apply {
            this.shouldNotLogPatterns.addAll(patterns.map { Pair(null, it) })
        }

        /**
         * Builds and returns a configured instance of [WebClientLoggingFilter].
         *
         * @return The configured [WebClientLoggingFilter] instance.
         */
        fun build(): WebClientLoggingFilter {
            return WebClientLoggingFilter(
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
