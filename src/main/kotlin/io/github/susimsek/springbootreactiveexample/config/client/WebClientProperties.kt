package io.github.susimsek.springbootreactiveexample.config.client

import io.github.susimsek.springbootreactiveexample.config.client.WebClientProperties.ClientConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for setting up `WebClient` in the application.
 *
 * This class binds properties from the application's configuration
 * related to `WebClient` settings, such as connection and read timeouts, and client-specific configurations.
 *
 * Example usage in `application.yml`:
 * ```yaml
 * spring:
 *   webclient:
 *     connectTimeout: 5000ms
 *     readTimeout: 10000ms
 *     clients:
 *       clientA:
 *         url: "https://api.clientA.com"
 *       clientB:
 *         url: "https://api.clientB.com"
 * ```
 *
 * @property connectTimeout
 *   The timeout duration for establishing a connection.
 * @property readTimeout
 *   The timeout duration for reading data from the connection.
 * @property clients
 *   A map of client-specific configurations, where each key is a client identifier
 *   and the value is its configuration..
 */
@ConfigurationProperties(prefix = "spring.webclient")
data class WebClientProperties(
    /**
     * The connection timeout duration.
     * Defaults to 5 seconds if not specified in configuration.
     */
    var connectTimeout: Duration? = Duration.ofSeconds(5),

    /**
     * The read timeout duration.
     * Defaults to 10 seconds if not specified in configuration.
     */
    var readTimeout: Duration? = Duration.ofSeconds(10),

    /**
     * A map of client-specific configurations.
     * Each entry represents a unique client, identified by a string key, and its corresponding [ClientConfig].
     */
    var clients: MutableMap<String, ClientConfig> = HashMap()
) {
    /**
     * Configuration for a specific WebClient instance.
     *
     * @property url The base URL for the client.
     */
    data class ClientConfig(
        var url: String
    )
}
