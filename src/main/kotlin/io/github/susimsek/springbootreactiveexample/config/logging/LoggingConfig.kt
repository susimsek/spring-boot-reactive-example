package io.github.susimsek.springbootreactiveexample.config.logging

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.susimsek.springbootreactiveexample.config.logging.enums.HttpLogLevel
import io.github.susimsek.springbootreactiveexample.config.logging.filter.LoggingFilter
import io.github.susimsek.springbootreactiveexample.config.logging.filter.WebClientLoggingFilter
import io.github.susimsek.springbootreactiveexample.config.logging.formatter.JsonLogFormatter
import io.github.susimsek.springbootreactiveexample.config.logging.formatter.LogFormatter
import io.github.susimsek.springbootreactiveexample.config.logging.utils.Obfuscator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod

/**
 * Configuration class that sets up HTTP logging for both client and server-side requests.
 *
 * This configuration class defines the beans necessary to enable detailed HTTP logging across the application.
 * It includes the setup of a logging filter for incoming HTTP requests (`LoggingFilter`) and outgoing requests
 * made using `WebClient` (`WebClientLoggingFilter`), as well as configurations for log formatting, obfuscation,
 * and log level management.
 *
 * Example usage:
 * ```kotlin
 * @Configuration
 * class ApplicationConfig {
 *     @Bean
 *     fun loggingConfig() = LoggingConfig()
 * }
 * ```
 */
@Configuration(proxyBeanMethods = false)
class LoggingConfig {

    /**
     * Provides a [LogFormatter] bean that formats logs into JSON format.
     *
     * @param objectMapper The [ObjectMapper] used for JSON serialization.
     * @return A [LogFormatter] instance that formats logs in JSON.
     */
    @Bean
    fun logFormatter(objectMapper: ObjectMapper): LogFormatter {
        return JsonLogFormatter(objectMapper)
    }

    /**
     * Provides an [Obfuscator] bean that masks sensitive data in HTTP headers, bodies, and query parameters.
     *
     * @param objectMapper The [ObjectMapper] used for JSON processing and masking.
     * @return An [Obfuscator] instance that handles sensitive data masking.
     */
    @Bean
    fun obfuscator(objectMapper: ObjectMapper): Obfuscator {
        return Obfuscator(objectMapper)
    }

    /**
     * Provides a [WebClientLoggingFilter] bean for logging HTTP requests made through `WebClient`.
     *
     * This filter captures and logs all outgoing `WebClient` requests while excluding specific paths from logging,
     * such as the `/todos` endpoint. The log level is set to `FULL` for detailed logging.
     *
     * @param logFormatter The [LogFormatter] used to format the log entries.
     * @param obfuscator The [Obfuscator] used to mask sensitive data.
     * @return A configured [WebClientLoggingFilter] instance.
     */
    @Bean
    fun loggingExchangeFilterFunction(
        logFormatter: LogFormatter,
        obfuscator: Obfuscator
    ): WebClientLoggingFilter {
        return WebClientLoggingFilter.builder(logFormatter, obfuscator)
            .httpLogLevel(HttpLogLevel.FULL) // Sets the log level to FULL for detailed logging.
            .shouldNotLog(HttpMethod.GET, "/todos") // Excludes /todos from logging.
            .build()
    }

    /**
     * Provides a [LoggingFilter] bean for logging incoming HTTP requests.
     *
     * This filter captures and logs all incoming HTTP requests, excluding static resources like JavaScript, CSS,
     * image files, and Swagger documentation. The log level is set to `FULL` for detailed logging.
     *
     * @param logFormatter The [LogFormatter] used to format the log entries.
     * @param obfuscator The [Obfuscator] used to mask sensitive data.
     * @return A configured [LoggingFilter] instance.
     */
    @Bean
    fun loggingFilter(
        logFormatter: LogFormatter,
        obfuscator: Obfuscator
    ): LoggingFilter {
        return LoggingFilter.builder(logFormatter, obfuscator)
            .httpLogLevel(HttpLogLevel.FULL) // Sets the log level to FULL for detailed logging.
            .shouldNotLog(
                "/webjars/**", // Excludes static resources like JavaScript, CSS, and images from logging.
                "/css/**",
                "/favicons/**",
                "/js/**",
                "/",
                "/images/**",
                "/*.html",
                "/*.js",
                "/*.css",
                "/*.ico",
                "/*.png",
                "/*.svg",
                "/*.webapp"
            )
            .shouldNotLog("/actuator/**") // Excludes actuator endpoints from logging.
            .shouldNotLog(
                "/swagger-ui.html", // Excludes Swagger UI and related endpoints from logging.
                "/swagger-ui/**",
                "/v3/api-docs/**"
            )
            .build()
    }
}
