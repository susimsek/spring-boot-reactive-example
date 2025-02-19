package io.github.susimsek.springbootreactiveexample.config.logging.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.http.HttpHeaders
import org.springframework.util.StringUtils
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

/**
 * A utility class for obfuscating sensitive data in HTTP logs.
 *
 * This class provides methods to mask sensitive headers, query parameters, and JSON body fields
 * to ensure privacy and security in logged HTTP requests and responses.
 *
 * Example usage:
 * ```
 * val obfuscator = Obfuscator(ObjectMapper())
 * val maskedHeaders = obfuscator.obfuscateHeaders(httpHeaders, listOf("Authorization", "Set-Cookie"))
 * val maskedBody = obfuscator.maskJsonBody(jsonBody, listOf("password", "ssn"))
 * val maskedUri = obfuscator.maskParameters(URI("https://example.com?token=123"), listOf("token"))
 * ```
 *
 * @property objectMapper The Jackson [ObjectMapper] instance used for JSON processing.
 */
class Obfuscator(
    private val objectMapper: ObjectMapper
) {

    /**
     * Masks sensitive HTTP headers by replacing their values with `******`.
     *
     * @param headers The original [HttpHeaders] containing potentially sensitive data.
     * @param sensitiveHeaders A list of header names to be masked.
     * @return A new [HttpHeaders] instance with masked sensitive values.
     */
    fun obfuscateHeaders(headers: HttpHeaders, sensitiveHeaders: List<String>): HttpHeaders {
        val masked = HttpHeaders()
        headers.forEach { (key, values) ->
            if (sensitiveHeaders.any { it.equals(key, ignoreCase = true) }) {
                masked[key] = listOf("******")
            } else {
                masked[key] = values
            }
        }
        return masked
    }

    /**
     * Masks sensitive fields in a JSON request or response body.
     *
     * - If the body is a valid JSON object, it searches for the specified `sensitivePaths`
     *   and replaces their values with `******`.
     * - If the body is not a JSON object, it is returned as-is.
     *
     * @param body The original JSON body as a string.
     * @param sensitivePaths A list of JSON field paths (e.g., `user.password`) to be masked.
     * @return The masked JSON string with sensitive values obfuscated.
     */
    fun maskJsonBody(body: String, sensitivePaths: List<String>): String {
        if (!StringUtils.hasText(body)) return body
        return try {
            val root = objectMapper.readTree(body)
            sensitivePaths.forEach { path ->
                val keys = path.split(".")
                maskPath(root, keys)
            }
            objectMapper.writeValueAsString(root)
        } catch (e: IOException) {
            body
        }
    }

    /**
     * Masks sensitive query parameters in a URI.
     *
     * - If the URI contains parameters listed in `sensitiveParameters`, their values are replaced with `******`.
     * - The rest of the URI remains unchanged.
     *
     * Example:
     * ```
     * Original: https://example.com?token=12345&user=admin
     * Masked:   https://example.com?token=******&user=admin
     * ```
     *
     * @param uri The original [URI] containing query parameters.
     * @param sensitiveParameters A list of parameter names to be masked.
     * @return A new [URI] instance with masked sensitive parameters.
     */
    fun maskParameters(uri: URI, sensitiveParameters: List<String>): URI {
        return try {
            val query = uri.query ?: return uri
            val maskedQuery = query.split("&").joinToString("&") { param ->
                val parts = param.split("=")
                if (parts.size == 2) {
                    val key = parts[0]
                    if (sensitiveParameters.any { it.equals(key, ignoreCase = true) }) {
                        "$key=******"
                    } else {
                        param
                    }
                } else {
                    param
                }
            }
            return URI(uri.scheme, uri.authority, uri.path, maskedQuery, uri.fragment)
        } catch (ex: URISyntaxException) {
            uri
        }
    }

    /**
     * Recursively traverses a JSON node and masks values at the specified paths.
     *
     * - If the key exists at the specified path, its value is replaced with `******`.
     * - Supports nested objects and arrays.
     *
     * @param node The JSON node to traverse.
     * @param keys The list of keys representing the path to mask.
     */
    private fun maskPath(node: JsonNode, keys: List<String>) {
        if (keys.isEmpty()) return
        val currentKey = keys.first()
        if (node.isObject) {
            val obj = node as ObjectNode
            if (keys.size == 1) {
                if (obj.has(currentKey)) {
                    obj.put(currentKey, "******")
                }
            } else {
                val child = obj.get(currentKey)
                if (child != null) {
                    maskPath(child, keys.drop(1))
                }
            }
        } else if (node.isArray) {
            node.forEach { element ->
                maskPath(element, keys)
            }
        }
    }
}
