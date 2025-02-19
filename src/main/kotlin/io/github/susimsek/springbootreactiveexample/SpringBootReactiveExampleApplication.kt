package io.github.susimsek.springbootreactiveexample

import io.github.susimsek.springbootreactiveexample.config.NativeConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints

/**
 * Entry point for the application.
 *
 * <p>This class initializes and runs the Spring Boot application.</p>
 *
 * <p>It is annotated with {@link SpringBootApplication}, which enables autoconfiguration,
 * component scanning, and additional Spring Boot configuration.</p>
 */
@SpringBootApplication
@ImportRuntimeHints(NativeConfig.AppNativeRuntimeHints::class)
class SpringBootReactiveExampleApplication

/**
 * Main method that serves as the entry point of the application.
 *
 * @param args command-line arguments passed to the application.
 */
fun main(args: Array<String>) {
    runApplication<SpringBootReactiveExampleApplication>(*args)
}
