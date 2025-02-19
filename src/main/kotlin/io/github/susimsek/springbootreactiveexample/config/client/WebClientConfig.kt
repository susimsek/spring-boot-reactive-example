package io.github.susimsek.springbootreactiveexample.config.client

import io.github.susimsek.springbootreactiveexample.config.logging.filter.WebClientLoggingFilter
import io.netty.channel.ChannelOption
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

/**
 * Configuration class for setting up WebClient and the associated TodoClient.
 *
 * This class provides the necessary beans for configuring WebClient with custom timeouts, logging filters,
 * and client-specific settings. It includes the setup for HTTP timeouts, WebClient filters, and customizations.
 * Additionally, it creates a `TodoClient` proxy based on WebClient, which is configured through application
 * properties.
 *
 * Example usage:
 * ```kotlin
 * @Configuration
 * class ApplicationConfig {
 *     @Bean
 *     fun webClientConfig() = WebClientConfig(webClientProperties)
 * }
 * ```
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebClientProperties::class)
class WebClientConfig(
    private val webClientProperties: WebClientProperties
) {

    /**
     * Builds and returns a [WebClient.Builder] for creating WebClient instances with custom configurations.
     *
     * - Configures connection and read timeouts.
     * - Applies logging filters for WebClient requests.
     * - Allows further customizations through [WebClientCustomizer].
     *
     * @param customizerProvider A provider for [WebClientCustomizer] instances to apply additional configurations.
     * @param webClientLoggingFilter A filter that logs HTTP requests and responses.
     * @return A configured [WebClient.Builder] instance.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun webClientBuilder(
        customizerProvider: ObjectProvider<WebClientCustomizer>,
        webClientLoggingFilter: WebClientLoggingFilter
    ): WebClient.Builder {
        val httpClient = HttpClient.create()
            .responseTimeout(webClientProperties.readTimeout)
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                webClientProperties.connectTimeout?.toMillis()?.toInt()
            )

        val builder = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .filter(webClientLoggingFilter)

        customizerProvider.orderedStream().forEach { customizer -> customizer.customize(builder) }

        return builder
    }
}
