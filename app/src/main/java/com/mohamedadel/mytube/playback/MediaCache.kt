package com.mohamedadel.mytube.playback

import android.content.Context
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

// Phase 1 — transient media cache (audio+video bytes, LRU-capped, user-clearable).
// Cache-first by design: CacheDataSource serves stored spans from disk and only
// fetches missing bytes upstream. YouTube URLs are signed + expiring, so a replay
// within the URL lifetime costs zero bytes; after expiry only a cheap re-resolve
// happens and bytes re-download. Never blocks pinning (Phase 2+): pins are separate files.
object MediaCache {
    @Volatile var maxBytes: Long = 500L * 1024 * 1024
        private set

    @Volatile private var cache: SimpleCache? = null
    private val lock = Any()

    fun get(context: Context): SimpleCache = synchronized(lock) {
        cache?.let { return it }
        val dir = File(context.cacheDir, "media")
        val built = SimpleCache(dir, LeastRecentlyUsedCacheEvictor(maxBytes), ExoDatabaseProvider(context))
        cache = built
        built
    }

    // Takes effect for subsequently built sources; in-flight playback keeps the old
    // instance until the next play (caller toasts accordingly).
    // Never warms here: SimpleCache init does disk I/O and this runs on the UI thread.
    // Callers must warm via warmAsync() from a background thread.
    fun setMax(bytes: Long) {
        maxBytes = bytes
        synchronized(lock) {
            try { cache?.release() } catch (_: Exception) { }
            cache = null
        }
    }
    fun warmAsync(context: Context) {
        try { get(context.applicationContext) } catch (_: Exception) { }
    }

    fun bytesUsed(context: Context): Long {
        return try { get(context.applicationContext).cacheSpace } catch (_: Exception) { 0L }
    }

    fun clear(context: Context) {
        synchronized(lock) {
            try { cache?.release() } catch (_: Exception) { }
            cache = null
            try {
                val dir = File(context.cacheDir, "media")
                if (dir.exists()) dir.deleteRecursively()
            } catch (_: Exception) { }
        }
    }

    // Wraps the app's DefaultDataSource upstream with the shared cache.
    // IGNORE_CACHE_ON_ERROR: a corrupt span falls back to network instead of failing playback.
    fun factory(context: Context, upstream: DataSource.Factory): CacheDataSource.Factory {
        val c = get(context.applicationContext)
        return CacheDataSource.Factory()
            .setCache(c)
            .setUpstreamDataSourceFactory(upstream)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
