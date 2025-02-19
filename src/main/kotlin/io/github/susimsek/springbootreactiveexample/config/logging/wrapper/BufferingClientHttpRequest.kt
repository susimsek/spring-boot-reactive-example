package io.github.susimsek.springbootreactiveexample.config.logging.wrapper

import io.github.susimsek.springbootreactiveexample.config.logging.utils.DataBufferCopyUtils
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.client.reactive.ClientHttpRequestDecorator
import reactor.core.publisher.Mono

/**
 * A decorator for [ClientHttpRequest] that buffers the request body to allow logging or other processing.
 *
 * This class wraps around the original [ClientHttpRequest] and intercepts the request body to
 * capture its content into a byte array. The captured body is then accessible via the `requestBody`
 * property for logging, auditing, or further manipulation before sending the request.
 *
 * Example usage:
 * ```
 * val decoratedRequest = BufferingClientHttpRequest(originalRequest)
 * val requestBody = decoratedRequest.requestBody // The captured request body
 * ```
 *
 * @param delegate The original [ClientHttpRequest] instance to be decorated.
 */
class BufferingClientHttpRequest(
    delegate: ClientHttpRequest
) : ClientHttpRequestDecorator(delegate) {

    /** The captured body of the HTTP request as a byte array. */
    var requestBody: ByteArray? = null
        private set

    /**
     * Intercepts and wraps the request body to buffer its content.
     *
     * The original `writeWith` method is called, but the body is first processed and copied
     * using [DataBufferCopyUtils.wrapAndBuffer], which stores the content into `requestBody`.
     *
     * @param body The body of the request as a [Publisher] of [DataBuffer].
     * @return A [Mono] that represents the completion of the body write operation.
     */
    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        return super.writeWith(bufferingWrap(body))
    }

    /**
     * Wraps the request body to capture its content and allow buffering.
     *
     * @param body The body of the request as a [Publisher] of [DataBuffer].
     * @return The wrapped [Publisher] that will buffer the request body.
     */
    private fun bufferingWrap(body: Publisher<out DataBuffer>): Publisher<out DataBuffer> {
        return io.github.susimsek.springbootreactiveexample.config.logging.utils.DataBufferCopyUtils.wrapAndBuffer(body) { copiedBody ->
            this.requestBody = copiedBody // Store the copied body into the `requestBody` property
        }
    }
}
