package io.github.susimsek.springbootreactiveexample.config.cache

import com.github.benmanes.caffeine.cache.AsyncCache
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentMap

/**
 * A generic coroutine-based cache manager that provides asynchronous caching operations.
 */
open class CoCacheManager<K, V>(
    private val cache: AsyncCache<K, V>
) : CacheManager<K, V> {

    private lateinit var cacheMap: ConcurrentMap<K, CompletableFuture<V>>
    private val logger = LoggerFactory.getLogger(CoCacheManager::class.java)

    @PostConstruct
    fun initCache() {
        cacheMap = cache.asMap()
    }

    /**
     * Retrieves a cached value asynchronously.
     *
     * @param key the key whose associated value is to be returned
     * @return the cached value or null if not present
     */
    override suspend fun get(key: K): V? {
        return cacheMap[key]?.await()?.also {
            logger.debug("Cache HIT - Key: {}", key)
        } ?: run {
            logger.debug("Cache MISS - Key: {}", key)
            null
        }
    }

    /**
     * Puts a value into the cache asynchronously.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be cached
     */
    override suspend fun put(key: K, value: V) {
        withContext(Dispatchers.Default) {
            cacheMap[key] = CompletableFuture.completedFuture(value)
        }
        logger.debug("Cache PUT - Key: {}", key)
    }

    /**
     * Removes a value from the cache asynchronously.
     *
     * @param key the key whose mapping is to be removed from the cache
     */
    override suspend fun evict(key: K) {
        cacheMap.remove(key)
        logger.debug("Cache EVICT - Key: {}", key)
    }

    /**
     * Clears all entries from the cache asynchronously.
     */
    override suspend fun clear() {
        cacheMap.clear()
        logger.debug("Cache CLEAR - All entries removed")
    }
}
