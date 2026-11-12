// Deferred item 8 (2026-11-11): periodic feed refresh behind a pref (default OFF).
// What it does: warms the 6h Room feed cache (FeedCacheEntity) for every subscribed channel
// (RSS first, videos-tab fallback — same sources as MainActivity.refreshFeed's oneChannel).
// It does NOT touch UI state: the next foreground open reads the warmed cache.
// Constraints: CONNECTED network only; WorkManager enforces the 15-minute minimum interval.
// Scheduling lives in MainActivity.syncFeedWorker (toggle in Settings > Downloads).
package com.mohamedadel.mytube.data.local

import android.content.Context
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class FeedRefreshWorker(appCtx: Context, params: WorkerParameters) : CoroutineWorker(appCtx, params) {
    override suspend fun doWork(): Result {
        return try {
            // pin-runtime 2026-11-11: same migration chain as MainActivity. The bare
            // builder used to throw (no migration to v9) on EVERY run -> Result.retry()
            // forever: the 15-min worker never succeeded and burned battery/radio.
            val db = Room.databaseBuilder(applicationContext, MyTubeDb::class.java, "mytube.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .build()
            val dao = try { db.playlists() } catch (_: Exception) { return Result.failure() }
            val subs = try { dao.observeSubscriptions().first() } catch (_: Exception) { emptyList() }
            if (subs.isEmpty()) return Result.success()
            // notif 2026-11-12: collect everything fresh for the bell (polls only when
            // pref_feed_auto is ON — enforced by schedule(), the 15m rule stays).
            val seen = mutableListOf<com.mohamedadel.mytube.data.youtube.VideoItem>()
            for (sub in subs) {
                try {
                    val id = YouTubeRepo.channelIdFor(sub.channelUrl) ?: continue
                    val fresh = try {
                        val rss = YouTubeRepo.feedFor(id)
                        if (rss.isNotEmpty()) rss else YouTubeRepo.channelVideos(sub.channelUrl).take(15)
                    } catch (_: Exception) { continue }
                    if (fresh.isEmpty()) continue
                    try { seen.addAll(fresh) } catch (_: Exception) { }
                    try { dao.upsertFeedCache(FeedCacheEntity(id, System.currentTimeMillis(), FeedJson.toJson(fresh))) } catch (_: Exception) { }
                } catch (_: Exception) { }
            }
            try { NotifCenter.recordFresh(dao, seen) } catch (_: Exception) { }
            try { db.close() } catch (_: Exception) { }
            Result.success()
        } catch (_: Exception) { Result.retry() }
    }

    companion object {
        const val TAG = "feed-refresh"
        fun schedule(appCtx: Context, enable: Boolean) {
            val wm = try { WorkManager.getInstance(appCtx) } catch (_: Exception) { return }
            if (!enable) {
                try { wm.cancelUniqueWork(TAG) } catch (_: Exception) { }
                return
            }
            val req = PeriodicWorkRequestBuilder<FeedRefreshWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(TAG)
                .build()
            try { wm.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, req) } catch (_: Exception) { }
        }
    }
}
