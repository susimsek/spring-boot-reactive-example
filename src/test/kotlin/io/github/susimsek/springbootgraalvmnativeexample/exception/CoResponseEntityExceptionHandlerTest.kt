package io.github.susimsek.springbootreactiveexample.exception

import io.github.susimsek.springbootreactiveexample.dto.Violation
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import jakarta.validation.constraints.NotNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.NotAcceptableStatusException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerErrorException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.server.UnsupportedMediaTypeStatusException

class CoResponseEntityExceptionHandlerTest {

  private class TestableExceptionHandler : CoResponseEntityExceptionHandler() {
    suspend fun testHandleNotAcceptableStatusException(
      ex: NotAcceptableStatusException,
      headers: HttpHeaders,
      status: HttpStatusCode,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleNotAcceptableStatusException(ex, headers, status, exchange)
    }

    suspend fun testHandleMethodNotAllowedException(
      ex: MethodNotAllowedException,
      headers: HttpHeaders,
      status: HttpStatusCode,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleMethodNotAllowedException(ex, headers, status, exchange)
    }

    suspend fun testHandleUnsupportedMediaTypeStatusException(
      ex: UnsupportedMediaTypeStatusException,
      headers: HttpHeaders,
      status: HttpStatusCode,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleUnsupportedMediaTypeStatusException(ex, headers, status, exchange)
    }

    suspend fun testHandleWebExchangeBindException(
      ex: WebExchangeBindException,
      headers: HttpHeaders,
      status: HttpStatusCode,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleWebExchangeBindException(ex, headers, status, exchange)
    }

    suspend fun testHandleConstraintViolationException(
      ex: ConstraintViolationException,
      headers: HttpHeaders,
      status: HttpStatus,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleConstraintViolationException(ex, headers, status, exchange)
    }

    suspend fun testHandleServerWebInputException(
      ex: ServerWebInputException,
      headers: HttpHeaders,
      status: HttpStatusCode,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleServerWebInputException(ex, headers, status, exchange)
    }

    suspend fun testHandleServerErrorException(
      ex: ServerErrorException,
      headers: HttpHeaders,
      status: HttpStatusCode,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleServerErrorException(ex, headers, status, exchange)
    }

    suspend fun testHandleResponseStatusException(
      ex: ResponseStatusException,
      headers: HttpHeaders,
      status: HttpStatusCode,
      exchange: ServerWebExchange
    ): ResponseEntity<Any> {
      return handleResponseStatusException(ex, headers, status, exchange)
    }
  }

  private val exceptionHandler = TestableExceptionHandler()

  @Test
  fun `should handle NotAcceptableStatusException and return 406 status with error details`() = runBlocking {
    // Arrange
    val exception = NotAcceptableStatusException(listOf(MediaType.APPLICATION_JSON))
    val mockExchange = Mockito.mock(ServerWebExchange::class.java)

    // Act
    val responseEntity = exceptionHandler.testHandleNotAcceptableStatusException(
      exception,
      HttpHeaders(),
      HttpStatus.NOT_ACCEPTABLE,
      mockExchange
    )

    // Assert
    assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.statusCode)
    val problemDetail = responseEntity.body as ProblemDetail
    assertEquals("not_acceptable", problemDetail.properties?.get("error"))
    assertEquals("Requested media type is not acceptable.", problemDetail.detail)
  }

  @Test
  fun `should handle MethodNotAllowedException and return 405 status with error details`() = runBlocking {
    // Arrange
    val unsupportedMethod = HttpMethod.POST
    val allowedMethods = listOf(HttpMethod.GET, HttpMethod.PUT)
    val exception = MethodNotAllowedException(unsupportedMethod, allowedMethods)
    val mockExchange = Mockito.mock(ServerWebExchange::class.java)

    // Act
    val responseEntity = exceptionHandler.testHandleMethodNotAllowedException(
      exception,
      HttpHeaders(),
      HttpStatus.METHOD_NOT_ALLOWED,
      mockExchange
    )

    // Assert
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.statusCode)
    val problemDetail = responseEntity.body as ProblemDetail
    assertEquals("method_not_allowed", problemDetail.properties?.get("error"))
    assertEquals("Requested HTTP method is not supported.", problemDetail.detail)
    assertEquals("GET, PUT", problemDetail.properties?.get("allowedMethods"))
  }

  @Test
  fun `should handle UnsupportedMediaTypeStatusException and return 415 status with error details`() = runBlocking {
    // Arrange
    val exception = UnsupportedMediaTypeStatusException(MediaType.APPLICATION_XML, listOf(MediaType.APPLICATION_JSON))
    val mockExchange = Mockito.mock(ServerWebExchange::class.java)

    // Act
    val responseEntity = exceptionHandler.testHandleUnsupportedMediaTypeStatusException(
      exception,
      HttpHeaders(),
      HttpStatus.UNSUPPORTED_MEDIA_TYPE,
      mockExchange
    )

    // Assert
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, responseEntity.statusCode)
    val problemDetail = responseEntity.body as ProblemDetail
    assertEquals("unsupported_media_type", problemDetail.properties?.get("error"))
    assertEquals("Requested media type is not supported.", problemDetail.detail)
    assertEquals("application/json", problemDetail.properties?.get("supportedMediaTypes"))
  }

  @Test
  fun `should handle WebExchangeBindException and return 400 status with validation errors`() = runBlocking {
    // Arrange
    val targetObject = Any()
    val bindingResult: BindingResult = BeanPropertyBindingResult(targetObject, "testObject")
    bindingResult.addError(FieldError("testObject", "field1", "must not be null"))
    bindingResult.addError(FieldError("testObject", "field2", "size must be between 3 and 50"))

    val methodParameter = Mockito.mock(MethodParameter::class.java)
    val exception = WebExchangeBindException(methodParameter, bindingResult)
    val mockExchange = Mockito.mock(ServerWebExchange::class.java)

    // Act
    val responseEntity = exceptionHandler.testHandleWebExchangeBindException(
      exception,
      HttpHeaders(),
      HttpStatus.BAD_REQUEST,
      mockExchange
    )

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)

    val problemDetail = responseEntity.body as ProblemDetail
    assertEquals("invalid_request", problemDetail.properties?.get("error"))
    assertEquals("Validation error occurred.", problemDetail.detail)

    @Suppress("UNCHECKED_CAST")
    val violations = problemDetail.properties?.get("violations") as List<io.github.susimsek.springbootreactiveexample.dto.Violation>
    assertEquals(2, violations.size)

    val violation1 = violations.first { it.field == "field1" }
    assertEquals("must not be null", violation1.message)

    val violation2 = violations.first { it.field == "field2" }
    assertEquals("size must be between 3 and 50", violation2.message)
  }

  @Test
  fun `should handle ServerWebInputException and return 400 status with error details`() = runBlocking {
    // Arrange
    val exception = ServerWebInputException("Invalid input provided")
    val mockExchange = Mockito.mock(ServerWebExchange::class.java)

    // Act
    val responseEntity = exceptionHandler.testHandleServerWebInputException(
      exception,
      HttpHeaders(),
      HttpStatus.BAD_REQUEST,
      mockExchange
    )

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)

    val problemDetail = responseEntity.body as ProblemDetail
    assertEquals("invalid_request", problemDetail.properties?.get("error"))
    assertEquals("Invalid input.", problemDetail.detail)
  }

  @Test
  fun `should handle ServerErrorException and return 500 status with error details`() = runBlocking {
    // Arrange
    val message = "Internal Server Error"
    val throwable = RuntimeException("Test cause")
    val exception = ServerErrorException(message, throwable)
    val mockExchange = Mockito.mock(ServerWebExchange::class.java)

    // Act
    val responseEntity = exceptionHandler.testHandleServerErrorException(
      exception,
      HttpHeaders(),
      HttpStatus.INTERNAL_SERVER_ERROR,
      mockExchange
    )

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)

    val problemDetail = responseEntity.body as ProblemDetail
    assertEquals("server_error", problemDetail.properties?.get("error"))
    assertEquals("A server error occurred while processing the request.", problemDetail.detail)
  }

  @Test
  fun `should handle ResponseStatusException and return appropriate status with error details`() = runBlocking {
    // Arrange
    val status = HttpStatus.FORBIDDEN
    val reason = "Access Denied"
    val exception = ResponseStatusException(status, reason)
    val mockExchange = Mockito.mock(ServerWebExchange::class.java)

    // Act
    val responseEntity = exceptionHandler.testHandleResponseStatusException(
      exception,
      HttpHeaders(),
      status,
      mockExchange
    )

    // Assert
    assertEquals(status, responseEntity.statusCode)

    val problemDetail = responseEntity.body as ProblemDetail
    assertEquals("response_status_exception", problemDetail.properties?.get("error"))
    assertEquals(reason, problemDetail.detail)
  }
}
