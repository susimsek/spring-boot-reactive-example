package io.github.susimsek.springbootreactiveexample.exception

import io.github.susimsek.springbootreactiveexample.dto.Violation
import jakarta.validation.ConstraintViolationException
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.NotAcceptableStatusException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerErrorException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.server.UnsupportedMediaTypeStatusException
import java.net.SocketTimeoutException
import java.util.*

/**
 * Base class for handling global exceptions in a Spring WebFlux application.
 * Provides default implementations for handling various HTTP exceptions.
 * Can be extended to provide custom behavior for specific exception types.
 */
abstract class CoResponseEntityExceptionHandler : MessageSourceAware {

    private var messageSource: MessageSource? = null

    /**
     * Sets the [MessageSource] to allow for localization of error messages.
     *
     * @param messageSource the [MessageSource] to be used.
     */
    override fun setMessageSource(messageSource: MessageSource) {
        this.messageSource = messageSource
    }

    /**
     * Retrieves the configured [MessageSource].
     *
     * @return the configured [MessageSource], or `null` if not set.
     */
    protected fun getMessageSource(): MessageSource? = this.messageSource

    private fun getLocale(exchange: ServerWebExchange): Locale {
        return exchange.localeContext.locale ?: Locale.getDefault()
    }

    protected fun getLocalizedMessage(key: String, exchange: ServerWebExchange): String {
        val locale = getLocale(exchange)
        return messageSource?.getMessage(key, null, locale) ?: key
    }

    protected fun getLocalizedMessage(key: String, exchange: ServerWebExchange, vararg args: Any?): String {
        val locale = getLocale(exchange)
        return messageSource?.getMessage(key, args, locale) ?: key
    }

    /**
     * Handles various exception types and maps them to appropriate HTTP responses.
     *
     * @param ex The exception to be handled.
     * @param exchange The current [ServerWebExchange].
     * @return A [ResponseEntity] containing the error details.
     */
    @ExceptionHandler(
        MethodNotAllowedException::class,
        NotAcceptableStatusException::class,
        UnsupportedMediaTypeStatusException::class,
        WebExchangeBindException::class,
        ServerWebInputException::class,
        ServerErrorException::class,
        ResponseStatusException::class,
        ConstraintViolationException::class,
        SocketTimeoutException::class,
        UnsupportedOperationException::class
    )
    suspend fun handleException(ex: Exception, exchange: ServerWebExchange): ResponseEntity<Any> {
        return when (ex) {
            is MethodNotAllowedException -> handleMethodNotAllowedException(
                ex,
                ex.headers,
                ex.statusCode,
                exchange
            )

            is NotAcceptableStatusException -> handleNotAcceptableStatusException(
                ex,
                ex.headers,
                ex.statusCode,
                exchange
            )

            is UnsupportedMediaTypeStatusException -> handleUnsupportedMediaTypeStatusException(
                ex,
                ex.headers,
                ex.statusCode,
                exchange
            )

            is WebExchangeBindException -> handleWebExchangeBindException(
                ex,
                ex.headers,
                ex.statusCode,
                exchange
            )

            is ConstraintViolationException -> handleConstraintViolationException(
                ex,
                HttpHeaders(),
                HttpStatus.BAD_REQUEST,
                exchange
            )

            is ServerWebInputException -> handleServerWebInputException(
                ex,
                ex.headers,
                ex.statusCode,
                exchange
            )

            is ServerErrorException -> handleServerErrorException(
                ex,
                ex.headers,
                ex.statusCode,
                exchange
            )

            is ResponseStatusException -> handleResponseStatusException(
                ex,
                ex.headers,
                ex.statusCode,
                exchange
            )

            is SocketTimeoutException -> handleSocketTimeoutException(
                ex,
                HttpHeaders(),
                HttpStatus.GATEWAY_TIMEOUT,
                exchange
            )

            is UnsupportedOperationException -> handleUnsupportedOperationException(
                ex,
                HttpHeaders(),
                HttpStatus.NOT_IMPLEMENTED,
                exchange
            )

            else -> createDefaultErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, exchange)
        }
    }

    /**
     * Handles [ServerErrorException].
     */
    protected open suspend fun handleServerErrorException(
        ex: ServerErrorException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.server_error", exchange)
        val problemDetail = createProblemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            detail = localizedDetail,
            errorCode = "server_error"
        )
        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [MethodNotAllowedException].
     */
    protected open suspend fun handleMethodNotAllowedException(
        ex: MethodNotAllowedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.method_not_allowed", exchange)
        val problemDetail = createProblemDetail(
            status = HttpStatus.METHOD_NOT_ALLOWED,
            detail = localizedDetail,
            errorCode = "method_not_allowed"
        ).apply {
            setProperty("allowedMethods", ex.supportedMethods.joinToString(", "))
        }
        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [NotAcceptableStatusException].
     */
    protected open suspend fun handleNotAcceptableStatusException(
        ex: NotAcceptableStatusException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.not_acceptable", exchange)
        val problemDetail = createProblemDetail(
            status = HttpStatus.NOT_ACCEPTABLE,
            detail = localizedDetail,
            errorCode = "not_acceptable"
        )
        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [UnsupportedMediaTypeStatusException].
     */
    protected open suspend fun handleUnsupportedMediaTypeStatusException(
        ex: UnsupportedMediaTypeStatusException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.unsupported_media_type", exchange)
        val problemDetail = createProblemDetail(
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            detail = localizedDetail,
            errorCode = "unsupported_media_type"
        ).apply {
            setProperty("supportedMediaTypes", ex.supportedMediaTypes.joinToString(", ") { it.toString() })
        }
        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [WebExchangeBindException].
     */
    protected open suspend fun handleWebExchangeBindException(
        ex: WebExchangeBindException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.validation_failed", exchange)

        val violations = ex.fieldErrors.map { Violation(it) } + ex.globalErrors.map { Violation(it) }

        val problemDetail = createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            detail = localizedDetail,
            errorCode = "invalid_request"
        ).apply {
            setProperty("violations", violations)
        }

        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [ConstraintViolationException].
     */
    protected open suspend fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.validation_failed", exchange)

        val violations = ex.constraintViolations.map { Violation(it) }

        val problemDetail = createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            detail = localizedDetail,
            errorCode = "invalid_request"
        ).apply {
            setProperty("violations", violations)
        }

        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [ServerWebInputException].
     */
    protected open suspend fun handleServerWebInputException(
        ex: ServerWebInputException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.invalid_request", exchange)

        val problemDetail = createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            detail = localizedDetail,
            errorCode = "invalid_request"
        )

        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [ResponseStatusException].
     */
    protected open suspend fun handleResponseStatusException(
        ex: ResponseStatusException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val problemDetail = createProblemDetail(
            status = ex.statusCode,
            detail = ex.reason ?: "Error occurred.",
            errorCode = "response_status_exception"
        )
        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Creates a default error response for uncaught exceptions.
     */
    private fun createDefaultErrorResponse(
        ex: Exception,
        status: HttpStatus,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.server_error", exchange)

        val problemDetail = createProblemDetail(
            status = status,
            detail = localizedDetail,
            errorCode = "server_error"
        )

        return ResponseEntity(problemDetail as Any, HttpHeaders(), status)
    }

    /**
     * Creates a [ProblemDetail] for the given parameters.
     */
    protected fun createProblemDetail(
        status: HttpStatusCode,
        detail: String,
        errorCode: String
    ): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(status, detail).apply {
            setProperty("error", errorCode)
        }
    }

    /**
     * Handles [SocketTimeoutException].
     */
    protected open suspend fun handleSocketTimeoutException(
        ex: SocketTimeoutException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.gateway_timeout", exchange)

        val problemDetail = createProblemDetail(
            status = HttpStatus.GATEWAY_TIMEOUT,
            detail = localizedDetail,
            errorCode = "gateway_timeout"
        )

        return ResponseEntity(problemDetail as Any, headers, status)
    }

    /**
     * Handles [UnsupportedOperationException].
     */
    protected open suspend fun handleUnsupportedOperationException(
        ex: UnsupportedOperationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.unsupported_operation", exchange)

        val problemDetail = createProblemDetail(
            status = HttpStatus.NOT_IMPLEMENTED,
            detail = localizedDetail,
            errorCode = "unsupported_operation"
        )

        return ResponseEntity(problemDetail as Any, headers, status)
    }
}
