package io.github.susimsek.springbootreactiveexample.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange

/**
 * Global exception handler for the application.
 *
 * This class provides centralized exception handling across all @RequestMapping methods
 * through @ExceptionHandler methods. It extends [CoResponseEntityExceptionHandler] to
 * inherit the base implementation and override specific behaviors.
 */
@ControllerAdvice
class GlobalExceptionHandler : CoResponseEntityExceptionHandler() {

    /**
     * Handles generic exceptions.
     *
     * This method is invoked whenever an unhandled exception occurs. It returns a response
     * with a generic error message and a `500 Internal Server Error` status code.
     *
     * @param ex the exception to handle.
     * @param exchange the current server web exchange.
     * @return a [ResponseEntity] containing problem details and HTTP status.
     */
    @ExceptionHandler(Exception::class)
    suspend fun handleGenericException(
        ex: Exception,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.server_error", exchange)

        val problemDetail = createProblemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            detail = localizedDetail,
            errorCode = "server_error"
        )

        return ResponseEntity(problemDetail as Any, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Handles ResourceNotFoundException.
     *
     * This method is invoked when a resource is not found. It returns a response
     * with a `404 Not Found` status code and an appropriate error message.
     *
     * @param ex the [ResourceNotFoundException] to handle.
     * @param exchange the current server web exchange.
     * @return a [ResponseEntity] containing problem details and HTTP status.
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    suspend fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.resource_not_found", exchange, *ex.getArgs())
        val problemDetail = createProblemDetail(
            status = HttpStatus.NOT_FOUND,
            detail = localizedDetail,
            errorCode = "resource_not_found"
        )
        return ResponseEntity(problemDetail as Any, HttpHeaders(), HttpStatus.NOT_FOUND)
    }

    /**
     * Handles ResourceConflictException.
     *
     * This method is invoked when a resource conflict occurs. It returns a response
     * with a `409 Conflict` status code and an appropriate error message.
     *
     * @param ex the [ResourceConflictException] to handle.
     * @param exchange the current server web exchange.
     * @return a [ResponseEntity] containing problem details and HTTP status.
     */
    @ExceptionHandler(ResourceConflictException::class)
    suspend fun handleResourceConflictException(
        ex: ResourceConflictException,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage("error.resource_conflict", exchange, *ex.getArgs())
        val problemDetail = createProblemDetail(
            status = HttpStatus.CONFLICT,
            detail = localizedDetail,
            errorCode = "resource_conflict"
        )
        return ResponseEntity(problemDetail as Any, HttpHeaders(), HttpStatus.CONFLICT)
    }

    /**
     * Handles ValidationException.
     *
     * This method is invoked when a validation error occurs. It returns a response
     * with a `400 Bad Request` status code and an appropriate error message.
     *
     * @param ex the [ValidationException] to handle.
     * @param exchange the current server web exchange.
     * @return a [ResponseEntity] containing problem details and HTTP status.
     */
    @ExceptionHandler(ValidationException::class)
    suspend fun handleValidationException(
        ex: ValidationException,
        exchange: ServerWebExchange
    ): ResponseEntity<Any> {
        val localizedDetail = getLocalizedMessage(
            ex.message ?: "Validation error occurred.",
            exchange,
            ex,
            *ex.getArgs()
        )
        val problemDetail = createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            detail = localizedDetail,
            errorCode = "invalid_request"
        )

        return ResponseEntity(problemDetail as Any, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }
}
