package io.github.susimsek.springbootreactiveexample.config.logging.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.susimsek.springbootreactiveexample.config.logging.enums.HttpLogType
import io.github.susimsek.springbootreactiveexample.config.logging.enums.Source
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import java.net.URI

/**
 * Represents an HTTP log entry containing request or response details.
 *
 * This data class is used to capture and store structured information about HTTP requests and responses,
 * including method, URI, status code, headers, body, source (client/server), and execution time.
 *
 * It is primarily used for logging HTTP interactions in a readable and structured format.
 *
 * Example usage:
 * ```
 * val httpLog = HttpLog(
 *     type = HttpLogType.REQUEST,
 *     method = HttpMethod.GET,
 *     uri = URI("https://example.com/api"),
 *     statusCode = 200,
 *     headers = HttpHeaders().apply { add("Authorization", "Bearer ***") },
 *     body = """{"username": "john_doe"}""",
 *     source = Source.CLIENT,
 *     durationMs = 150
 * )
 * ```
 *
 * @property type The type of HTTP log (request or response).
 * @property method The HTTP method used (GET, POST, etc.).
 * @property uri The full URI of the request.
 * @property statusCode The HTTP status code (only applicable for responses).
 * @property headers The HTTP headers associated with the request/response.
 * @property body The request or response body (may be `null`).
 * @property source The source of the HTTP request (client or server).
 * @property durationMs The time taken to complete the request in milliseconds.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class HttpLog(
    @JsonProperty
    var type: HttpLogType,

    @JsonProperty
    var method: HttpMethod,

    @JsonProperty
    var uri: URI,

    @JsonProperty
    var statusCode: Int?,

    @JsonProperty
    var headers: HttpHeaders?,

    @JsonProperty
    var body: String?,

    @JsonProperty
    var source: Source,

    @JsonProperty
    var durationMs: Long?
)
