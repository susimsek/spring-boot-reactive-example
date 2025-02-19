package io.github.susimsek.springbootreactiveexample.config.logging.utils

import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory

/**
 * Utility class for safely copying and buffering `DataBuffer` streams in reactive pipelines.
 *
 * This class helps in capturing and processing HTTP request/response bodies by converting
 * `DataBuffer` streams into byte arrays while ensuring proper memory management.
 *
 * Example usage:
 * ```
 * val loggedBody = DataBufferCopyUtils.wrapAndBuffer(dataBufferStream) { bytes ->
 *     val bodyAsString = String(bytes, StandardCharsets.UTF_8)
 *     logger.info("Captured Body: $bodyAsString")
 * }
 * ```
 */
object DataBufferCopyUtils {

    /**
     * Wraps and buffers a `Publisher<DataBuffer>`, ensuring the data can be read and copied.
     *
     * - Joins multiple `DataBuffer` chunks into a single buffer.
     * - Copies the byte data for logging or processing.
     * - Ensures proper memory management by releasing buffers after reading.
     *
     * @param body The `Publisher<DataBuffer>` stream to be processed.
     * @param copyConsumer A function that consumes the copied byte array (e.g., for logging).
     * @return A new `Publisher<DataBuffer>` containing the buffered data.
     */
    fun wrapAndBuffer(body: Publisher<out DataBuffer>, copyConsumer: (ByteArray) -> Unit): Publisher<out DataBuffer> {
        return DataBufferUtils
            .join(body)
            .defaultIfEmpty(DefaultDataBufferFactory.sharedInstance.wrap(ByteArray(0)))
            .map { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)
                val wrappedDataBuffer: DefaultDataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(bytes)
                copyConsumer(bytes)
                wrappedDataBuffer
            }
    }
}
