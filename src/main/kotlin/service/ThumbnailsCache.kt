package service

import Constants
import javax.swing.Icon
import java.util.concurrent.ConcurrentHashMap

// Save on device or destroy after each session?
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

    fun size(): Int {
        synchronized(lock) {
            return cache.size
        }
    }

    fun invalidate() {
        cache.clear()
        accessOrder.clear()
    }

    private fun trimToSize() {
        while (accessOrder.size > maxSize) {
            val leastRecentlyUsed = accessOrder.removeAt(0)
            cache.remove(leastRecentlyUsed)
        }
    }
}
