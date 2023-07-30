package service

import Constants
import javax.swing.Icon
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton object that manages a thread-safe cache for storing generated thumbnails.
 *
 * This cache uses the Least Recently Used (LRU) policy: when the cache reaches its maximum size,
 * the least recently accessed thumbnails are removed to make room for new ones.
 *
 * The cache is implemented using a ConcurrentHashMap for storing the thumbnails and a
 * MutableList for tracking the access order of the thumbnails.
 * Synchronization is used to ensure that the cache operations are thread-safe.
 *
 * Note: not sure whether the cache should persist on the device or to be
 * destroyed after each session.
 */

object ThumbnailsCache {
    private const val maxSize: Int = Constants.THUMBNAIL_CACHE_SIZE
    private val cache: ConcurrentHashMap<String, Icon> = ConcurrentHashMap()
    private val accessOrder = mutableListOf<String>()
    private val lock = Any()

    operator fun get(path: String): Icon? {
        synchronized(lock) {
            accessOrder.remove(path)
            accessOrder.add(path)
            trimToSize()
            return cache[path]
        }
    }

    operator fun set(path: String, icon: Icon) {
        synchronized(lock) {
            cache[path] = icon
            accessOrder.remove(path)
            accessOrder.add(path)
            trimToSize()
        }
    }

    private fun trimToSize() {
        while (accessOrder.size > maxSize) {
            val leastRecentlyUsed = accessOrder.removeAt(0)
            cache.remove(leastRecentlyUsed)
        }
    }

    // The methods below are to be used only in tests!
    fun size(): Int {
        synchronized(lock) {
            return cache.size
        }
    }

    fun invalidate() {
        cache.clear()
        accessOrder.clear()
    }
}
