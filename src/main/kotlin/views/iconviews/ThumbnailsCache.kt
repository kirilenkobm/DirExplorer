package views.iconviews

import javax.swing.Icon
import java.util.concurrent.ConcurrentHashMap

// TODO: ensure whether it belongs here, but not state package
// TODO: work with zip archives
// Save on device or destroy after each session?
object ThumbnailsCache {
    private const val maxSize: Int = 10000
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
}
