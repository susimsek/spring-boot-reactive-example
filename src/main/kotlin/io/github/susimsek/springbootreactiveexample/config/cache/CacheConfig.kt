package io.github.susimsek.springbootreactiveexample.config.cache

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.susimsek.springbootreactiveexample.dto.GreetingDTO
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures caching for the application using Caffeine, a high-performance caching library.
 *
 * This class loads cache settings from application properties via [CacheProperties]
 * and initializes cache instances based on the defined configurations.
 */
@Configuration
@EnableConfigurationProperties(CacheProperties::class)
class CacheConfig(private val cacheProperties: CacheProperties) {

    /**
     * Creates and configures the `greetingCache` bean.
     *
     * The cache properties such as expiration time, initial capacity, and maximum size
     * are dynamically loaded from application configurations.
     *
     * @return an instance of [AsyncCache] for storing greeting messages.
     */
    @Bean
    fun greetingCache(): AsyncCache<String, GreetingDTO> {
        val config = cacheProperties.caches["greetingCache"] ?: cacheProperties.defaultConfig

        return Caffeine.newBuilder()
            .expireAfterWrite(config.ttl) // Expiration time
            .initialCapacity(config.initialCapacity) // Initial capacity
            .maximumSize(config.maximumSize) // Maximum number of entries
            .buildAsync()
    }
}
