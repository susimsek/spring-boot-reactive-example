package io.github.susimsek.springbootreactiveexample.config.cache

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for cache settings.
 *
 * This class allows defining default cache configurations and specific cache settings
 * that can be customized through application properties (e.g., `application.yml` or `application.properties`).
 */
@ConfigurationProperties(prefix = "spring.cache")
data class CacheProperties(

    /**
     * Default cache configuration that applies to all caches unless overridden.
     */
    var defaultConfig: CacheConfig = CacheConfig(),

    /**
     * List of cache names to be managed by the application.
     */
    var cacheNames: List<String> = listOf(),

    /**
     * Custom cache configurations for specific cache names.
     * Allows defining different settings for individual caches.
     */
    var caches: MutableMap<String, CacheConfig> = mutableMapOf()
) {
    /**
     * Configuration settings for individual caches.
     *
     * Each cache can have its own TTL (Time-To-Live), capacity, and optional refresh settings.
     */
    data class CacheConfig(

        /**
         * Time-to-live (TTL) duration for cache entries.
         * Determines how long items should remain in the cache before expiring.
         * Default is 1 hour.
         */
        var ttl: Duration = Duration.ofHours(1),

        /**
         * Initial capacity of the cache.
         * Defines how many items the cache can initially store before resizing.
         * Default is 1000 items.
         */
        var initialCapacity: Int = 1000,

        /**
         * Maximum number of items allowed in the cache.
         * Once the cache reaches this limit, the least recently used items are evicted.
         * Default is 10,000 items.
         */
        var maximumSize: Long = 10000L,
    )
}
