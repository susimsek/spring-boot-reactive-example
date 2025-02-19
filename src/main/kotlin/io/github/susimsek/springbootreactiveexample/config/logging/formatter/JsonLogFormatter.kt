package io.github.susimsek.springbootreactiveexample.config.logging.formatter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.susimsek.springbootreactiveexample.config.logging.model.HttpLog
import org.springframework.http.HttpHeaders
import org.springframework.util.StringUtils
import java.io.IOException

/**
 * A JSON-based implementation of [LogFormatter] that formats HTTP logs in a structured JSON format.
 *
 * This formatter converts an [HttpLog] instance into a JSON representation using [ObjectMapper].
 * It ensures proper formatting of request/response details, including headers, body, and execution time.
 *
 * Example output:
 * ```json
 * {
 *   "source": "client",
 *   "type": "request",
 *   "method": "POST",
 *   "uri": "https://example.com/api",
 *   "host": "example.com",
 *   "path": "/api",
 *   "statusCode": 200,
 *   "duration": "150ms",
 *   "headers": {
 *     "Authorization": "Bearer ***",
 *     "Content-Type": "application/json"
 *   },
 *   "body": {
 *     "username": "john_doe",
 *     "password": "***"
 *   }
 * }
 * ```
 *
 * @property objectMapper The Jackson [ObjectMapper] instance used for JSON processing.
 */
class JsonLogFormatter(private val objectMapper: ObjectMapper) : LogFormatter {

    /**
     * Formats an [HttpLog] instance into a structured JSON string.
     *
     * The formatted log includes metadata such as method, URI, headers, response status,
     * execution duration, and request/response bodies.
     *
     * @param httpLog The HTTP log entry to be formatted.
     * @return A JSON string representation of the HTTP log.
     */
    override fun format(httpLog: HttpLog): String {
        val logNode: ObjectNode = objectMapper.createObjectNode()
        logNode.put("source", httpLog.source.toString().lowercase())
        logNode.put("type", httpLog.type.toString().lowercase())
        logNode.put("method", httpLog.method.name())
        logNode.put("uri", httpLog.uri.toString())
        logNode.put("host", httpLog.uri.host)
        logNode.put("path", httpLog.uri.path)

        httpLog.durationMs?.let { logNode.put("duration", "${it}ms") }
        httpLog.statusCode?.let { logNode.put("statusCode", it) }
        httpLog.headers?.let { logNode.set<JsonNode>("headers", parseHeaders(it)) }

        if (StringUtils.hasText(httpLog.body)) {
            logNode.set<JsonNode>("body", parseBody(httpLog.body!!))
        }

        return logNode.toPrettyString()
    }

    /**
     * Converts HTTP headers into a JSON format.
     *
     * @param headers The HTTP headers to be serialized.
     * @return A JSON node representing the headers.
     */
    private fun parseHeaders(headers: HttpHeaders): JsonNode {
        return objectMapper.valueToTree(headers)
    }

    /**
     * Parses the request/response body and attempts to convert it to a JSON format.
     *
     * If the body is a valid JSON string, it is parsed as a [JsonNode]. Otherwise,
     * it is logged as plain text.
     *
     * @param bodyString The HTTP body content as a string.
     * @return A [JsonNode] representing the parsed body.
     */
    private fun parseBody(bodyString: String): JsonNode {
        return try {
            objectMapper.readTree(bodyString)
        } catch (e: IOException) {
            val node = objectMapper.createObjectNode()
            node.put("body", bodyString) // Not a JSON body, log as plain text
            node
        }
    }
}
