package com.mohamedadel.mytube

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

// C1: single tuned Coil loader for the whole app (thumbnails, avatars, banners).
// 25% memory + 100MB disk + crossfade: returning to a list re-reads from disk,
// not from the network. Transient and user-clearable via the media-cache button
// (shares cacheDir; media bytes live in cacheDir/media, images in cacheDir/images).
class MyTubeApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Gate-2: debug-only StrictMode — logs main-thread IO leaks for future work, never crashes.
        if (com.mohamedadel.mytube.BuildConfig.DEBUG) {
            try {
                android.os.StrictMode.setThreadPolicy(
                    android.os.StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build()
                )
                android.os.StrictMode.setVmPolicy(
                    android.os.StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build()
                )
            } catch (_: Exception) { }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("images"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
