package io.github.susimsek.springbootreactiveexample.config.cache

/**
 * A generic cache manager interface that provides basic cache operations.
 * Implementing classes should define their own caching mechanisms.
 *
 * @param K the type of keys maintained by the cache
 * @param V the type of values stored in the cache
 */
interface CacheManager<K, V> {
    /**
     * Retrieves a value from the cache by its key.
     *
     * @param key the key whose associated value is to be returned
     * @return the cached value, or null if not present
     */
    suspend fun get(key: K): V?

    /**
     * Stores a key-value pair in the cache.
     *
     * @param key the key to associate with the specified value
     * @param value the value to be stored in the cache
     */
    suspend fun put(key: K, value: V)

    /**
     * Removes a specific entry from the cache.
     *
     * @param key the key whose mapping is to be removed
     */
    suspend fun evict(key: K)

    /**
     * Clears all entries from the cache.
     */
    suspend fun clear()
}
