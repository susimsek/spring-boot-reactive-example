package io.github.susimsek.springbootreactiveexample.exception

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.server.ServerWebExchange
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {

    private val globalExceptionHandler = GlobalExceptionHandler()

    @Test
    fun `should handle generic exception and return 500 status with error details`() {
        runBlocking {
            // Arrange
            val exception = Exception("Test exception")
            val mockExchange = Mockito.mock(ServerWebExchange::class.java)

            // Act
            val responseEntity = globalExceptionHandler.handleGenericException(exception, mockExchange)

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.statusCode)

            val problemDetail = responseEntity.body as ProblemDetail
            val error = problemDetail.properties?.get("error")
            assertEquals("server_error", error)
            assertEquals("An internal server error occurred. Please try again later.", problemDetail.detail)
        }
    }

    @Test
    fun `should handle ResourceNotFoundException and return 404 status with error details`() {
        runBlocking {
            // Arrange
            val exception = ResourceNotFoundException("User", "ID", 123)
            val mockExchange = Mockito.mock(ServerWebExchange::class.java)

            // Act
            val responseEntity = globalExceptionHandler.handleResourceNotFoundException(exception, mockExchange)

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)

            val problemDetail = responseEntity.body as ProblemDetail
            val error = problemDetail.properties?.get("error")
            assertEquals("resource_not_found", error)
            assertEquals("The User not found with ID: 123", problemDetail.detail)
        }
    }

    @Test
    fun `should handle ResourceConflictException and return 409 status with error details`() {
        runBlocking {
            // Arrange
            val exception = ResourceConflictException("User", "Email", "example@example.com")
            val mockExchange = Mockito.mock(ServerWebExchange::class.java)

            // Act
            val responseEntity = globalExceptionHandler.handleResourceConflictException(exception, mockExchange)

            // Assert
            assertEquals(HttpStatus.CONFLICT, responseEntity.statusCode)

            val problemDetail = responseEntity.body as ProblemDetail
            val error = problemDetail.properties?.get("error")
            assertEquals("resource_conflict", error)
            assertEquals("The User already exists with Email: example@example.com", problemDetail.detail)
        }
    }

    @Test
    fun `should handle ValidationException and return 400 status with error details`() {
        runBlocking {
            // Arrange
            val exception = ValidationException("Invalid input: Name cannot be empty")
            val mockExchange = Mockito.mock(ServerWebExchange::class.java)

            // Act
            val responseEntity = globalExceptionHandler.handleValidationException(exception, mockExchange)

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)

            val problemDetail = responseEntity.body as ProblemDetail
            val error = problemDetail.properties?.get("error")
            assertEquals("invalid_request", error)
            assertEquals("Invalid input: Name cannot be empty", problemDetail.detail)
        }
    }
}
