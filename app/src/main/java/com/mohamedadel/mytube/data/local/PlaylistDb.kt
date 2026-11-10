package com.mohamedadel.mytube.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mohamedadel.mytube.data.youtube.VideoItem
import kotlinx.coroutines.flow.Flow

// A named, user-saved queue.
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🎵",
    val createdAt: Long = System.currentTimeMillis(),
    // "music" (audio-first) | "video" (forces a video quality on play-all/from, so
    // video playlists don't crowd the music flow). V9 column, default keeps old rows musical.
    val mode: String = "music"
)

// One video inside a playlist. `position` defines the play order.
@Entity(
    tableName = "playlist_items",
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("playlistId")]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val videoId: String?,
    val title: String?,
    val channel: String?,
    val thumbnail: String?,
    val url: String?,
    val durationSec: Long?,
    val viewCount: Long?,
    val position: Int
)

// One-tap favorite. Keyed by stable video key (id or url) so toggling is idempotent.
@Entity(tableName = "favorites", indices = [Index(value = ["videoKey"], unique = true)])
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoKey: String,
    val videoId: String?,
    val title: String?,
    val channel: String?,
    val thumbnail: String?,
    val url: String?,
    val durationSec: Long?,
    val viewCount: Long?,
    val addedAt: Long = System.currentTimeMillis()
)

// Listening history: one row per video, refreshed on every play (REPLACE keeps it unique),
// capped to the latest 100 by the writer.
@Entity(tableName = "history", indices = [Index(value = ["videoKey"], unique = true)])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoKey: String,
    val videoId: String?,
    val title: String?,
    val channel: String?,
    val thumbnail: String?,
    val url: String?,
    val durationSec: Long?,
    val viewCount: Long?,
    val playedAt: Long = System.currentTimeMillis()
)

// Subscriptions (local-only, no account): channel URL is the stable key.
@Entity(tableName = "subscriptions", indices = [Index(value = ["channelUrl"], unique = true)])
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelUrl: String,
    val name: String?,
    val avatar: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

// Channel groups: user-named buckets of subscribed channels for browsing/management.
// Members reference subscriptions by channelUrl (stable key); deleting a subscription
// orphans the member row harmlessly (feed join simply finds no channel).
@Entity(tableName = "channel_groups")
data class ChannelGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "channel_group_members",
    indices = [Index(value = ["groupId"]), Index(value = ["channelUrl"])],
    foreignKeys = [androidx.room.ForeignKey(
        entity = ChannelGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )]
)
data class ChannelGroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val channelUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)

// Offline pins ("download for offline mode"): faithful snapshot descriptor.
// Bytes live in filesDir/travel/<sha256(videoKey)>/ (video.mp4/audio.m4a/sub.vtt/meta.json).
// This row is the index: size + labels for the storage UI, badges and filter tabs.
@Entity(tableName = "pins", indices = [Index(value = ["videoKey"], unique = true)])
data class PinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoKey: String,
    val videoId: String?,
    val title: String?,
    val channel: String?,
    val url: String?,
    val kind: String = "audio", // "audio" | "video"
    val quality: String?,
    val audioTag: String?,
    val subTag: String?,
    val sizeBytes: Long = 0,
    val pinnedAt: Long = System.currentTimeMillis()
)

// Playlist row + item count for the management screen.
data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val emoji: String,
    val createdAt: Long,
    val count: Int,
    val mode: String = "music"
)

@Dao
interface PlaylistDao {
    @Query("SELECT p.id AS id, p.name AS name, p.emoji AS emoji, p.createdAt AS createdAt, COUNT(i.id) AS count, p.mode AS mode FROM playlists p LEFT JOIN playlist_items i ON i.playlistId = p.id GROUP BY p.id ORDER BY p.createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :pid ORDER BY position ASC")
    fun observeItems(pid: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :pid ORDER BY position ASC")
    suspend fun itemsOnce(pid: Long): List<PlaylistItemEntity>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :pid")
    suspend fun itemCount(pid: Long): Int

    @Query("DELETE FROM playlist_items WHERE playlistId = :pid AND id IN (:ids)")
    suspend fun deleteItems(pid: Long, ids: List<Long>)

    // Playlist cover from its songs' artwork (no stored cover, no migration): first 4 thumbs.
    @Query("SELECT thumbnail FROM playlist_items WHERE playlistId = :pid AND thumbnail IS NOT NULL AND thumbnail != '' ORDER BY position ASC LIMIT 4")
    suspend fun playlistThumbs(pid: Long): List<String>

    @Insert
    suspend fun insertPlaylist(p: PlaylistEntity): Long

    @Insert
    suspend fun insertItems(items: List<PlaylistItemEntity>)

    @Query("UPDATE playlists SET name = :name WHERE id = :pid")
    suspend fun rename(pid: Long, name: String)

    @Query("UPDATE playlists SET emoji = :emoji WHERE id = :pid")
    suspend fun setEmoji(pid: Long, emoji: String)

    @Query("DELETE FROM playlists WHERE id = :pid")
    suspend fun deletePlaylist(pid: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :pid AND id = :iid")
    suspend fun deleteItem(pid: Long, iid: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :pid")
    suspend fun clearPlaylist(pid: Long)

    @Update
    suspend fun updateItems(items: List<PlaylistItemEntity>)

    // Staged editor save as ONE transaction: rename + emoji + deletions + reorder succeed or fail together.
    @Query("UPDATE playlists SET mode = :mode WHERE id = :pid")
    suspend fun setMode(pid: Long, mode: String)

    @Transaction
    suspend fun savePlaylistEdits(pid: Long, name: String, emoji: String, mode: String, orderedIds: List<Long>) {
        if (name.isNotBlank()) rename(pid, name)
        setEmoji(pid, emoji.ifBlank { "🎵" })
        setMode(pid, if (mode == "video") "video" else "music")
        val rows = itemsOnce(pid)
        rows.filter { it.id !in orderedIds }.forEach { deleteItem(pid, it.id) }
        val keep = orderedIds.mapNotNull { id -> rows.firstOrNull { it.id == id } }
        updateItems(keep.mapIndexed { k, r -> r.copy(position = k) })
    }

    // ---- Favorites (one-tap heart) ----
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT videoKey FROM favorites")
    fun observeFavoriteKeys(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE videoKey = :key")
    suspend fun favoriteCount(key: String): Int

    @Insert
    suspend fun insertFavorite(f: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE videoKey = :key")
    suspend fun deleteFavorite(key: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    // ---- Listening history ----
    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT :limit")
    fun observeHistory(limit: Int = 100): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(h: HistoryEntity): Long

    @Query("DELETE FROM history WHERE videoKey = :key")
    suspend fun deleteHistory(key: String)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    // H1: Paging 3 over manual LIMIT/OFFSET pages (room-paging artifact unavailable offline;
    // app-level PagingSource in HistoryPaging.kt drives the same lazy UX + LoadStates).
    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun historyPage(limit: Int, offset: Int): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE videoKey IN (:keys) ORDER BY playedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun historyPagePinned(keys: List<String>, limit: Int, offset: Int): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE videoKey NOT IN (:keys) ORDER BY playedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun historyPageUnpinned(keys: List<String>, limit: Int, offset: Int): List<HistoryEntity>

    // R2: local search (title/channel, newest first, capped — paging unnecessary for matches)
    // M2: caller passes the pattern pre-escaped; ESCAPE keeps user %/_ literal.
    @Query("SELECT * FROM history WHERE title LIKE :q ESCAPE '\\' OR channel LIKE :q ESCAPE '\\' ORDER BY playedAt DESC LIMIT 200")
    suspend fun historySearch(q: String): List<HistoryEntity>

    @Query("SELECT COUNT(*) FROM history")
    suspend fun historyCount(): Int

    @Query("SELECT videoKey, videoId, title, channel, thumbnail, url, COUNT(*) AS plays, MAX(playedAt) AS lastPlayed FROM history GROUP BY videoKey ORDER BY plays DESC, lastPlayed DESC LIMIT :limit")
    suspend fun topPlayed(limit: Int): List<PlayedStat>

    // Cap 1000 rows (~200KB): Paging makes the full depth browsable, the cap only bounds disk.
    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY playedAt DESC LIMIT 1000)")
    suspend fun trimHistory()

// ---- Subscriptions ----
    @Query("SELECT * FROM subscriptions ORDER BY addedAt DESC")
    fun observeSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT COUNT(*) FROM subscriptions WHERE channelUrl = :url")
    suspend fun subscriptionCount(url: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubscription(s: SubscriptionEntity): Long

    @Query("DELETE FROM subscriptions WHERE channelUrl = :url")
    suspend fun deleteSubscription(url: String)

    // ---- Channel groups ----
    @Query("SELECT * FROM channel_groups ORDER BY createdAt ASC")
    fun observeGroups(): Flow<List<ChannelGroupEntity>>

    @Insert
    suspend fun insertGroup(g: ChannelGroupEntity): Long

    @Query("UPDATE channel_groups SET name = :name WHERE id = :id")
    suspend fun renameGroup(id: Long, name: String)

    @Query("DELETE FROM channel_groups WHERE id = :id")
    suspend fun deleteGroup(id: Long)

    @Query("SELECT * FROM channel_group_members WHERE groupId = :gid ORDER BY addedAt ASC")
    fun observeGroupMembers(gid: Long): Flow<List<ChannelGroupMemberEntity>>

    @Query("SELECT * FROM channel_group_members WHERE channelUrl = :url")
    suspend fun memberRows(url: String): List<ChannelGroupMemberEntity>

    @Query("SELECT COUNT(*) FROM channel_group_members WHERE groupId = :gid")
    suspend fun memberCount(gid: Long): Int

    @Query("SELECT channelUrl FROM channel_group_members WHERE groupId = :gid")
    suspend fun memberUrls(gid: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMember(m: ChannelGroupMemberEntity): Long

    @Query("DELETE FROM channel_group_members WHERE groupId = :gid AND channelUrl = :url")
    suspend fun removeMember(gid: Long, url: String)

    // ---- Offline pins ----
    @Query("SELECT * FROM pins ORDER BY pinnedAt DESC")
    fun observePins(): Flow<List<PinEntity>>

    @Query("SELECT videoKey FROM pins")
    fun observePinKeys(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM pins WHERE videoKey = :key")
    suspend fun pinCount(key: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPin(p: PinEntity): Long

    @Query("DELETE FROM pins WHERE videoKey = :key")
    suspend fun deletePin(key: String)

    @Query("DELETE FROM pins")
    suspend fun clearPins()

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM pins")
    suspend fun pinsTotalSize(): Long

    // ---- Feed cache ----
    @Query("SELECT * FROM feed_cache WHERE channelId = :id")
    suspend fun getFeedCache(id: String): FeedCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeedCache(e: FeedCacheEntity)

    @Query("DELETE FROM feed_cache")
    suspend fun clearFeedCache()
}
// Local taste profile (recommendations without an account): repeat-play ranking.
// Query-only: no schema change, no migration.
data class PlayedStat(
    val videoKey: String,
    val videoId: String?,
    val title: String?,
    val channel: String?,
    val thumbnail: String?,
    val url: String?,
    val plays: Long,
    val lastPlayed: Long
)


@Database(entities = [PlaylistEntity::class, PlaylistItemEntity::class, FavoriteEntity::class, HistoryEntity::class, SubscriptionEntity::class, PinEntity::class, FeedCacheEntity::class, ChannelGroupEntity::class, ChannelGroupMemberEntity::class], version = 9, exportSchema = false)
abstract class MyTubeDb : RoomDatabase() {
    abstract fun playlists(): PlaylistDao
}

// v2 -> v3: favorites table (non-destructive).
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS favorites (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, videoKey TEXT NOT NULL, videoId TEXT, title TEXT, channel TEXT, thumbnail TEXT, url TEXT, durationSec INTEGER, viewCount INTEGER, addedAt INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_favorites_videoKey ON favorites (videoKey)")
    }
}

// v3 -> v4: listening-history table (non-destructive).
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, videoKey TEXT NOT NULL, videoId TEXT, title TEXT, channel TEXT, thumbnail TEXT, url TEXT, durationSec INTEGER, viewCount INTEGER, playedAt INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_history_videoKey ON history (videoKey)")
    }
}

// v4 -> v5: subscriptions table (non-destructive).
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS subscriptions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, channelUrl TEXT NOT NULL, name TEXT, avatar TEXT, addedAt INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_subscriptions_channelUrl ON subscriptions (channelUrl)")
    }
}

// L2: feed cache (per-channel VideoItem snapshot as JSON, TTL-checked by caller).
// Skips Room TypeConverters: one TEXT column, manual org.json mapping (no new deps).
@Entity(tableName = "feed_cache")
data class FeedCacheEntity(
    @PrimaryKey val channelId: String,
    val fetchedAt: Long = System.currentTimeMillis(),
    val itemsJson: String = "[]"
)

// v5 -> v6: offline pins index (non-destructive; bytes live in filesDir/travel/).
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS pins (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, videoKey TEXT NOT NULL, videoId TEXT, title TEXT, channel TEXT, url TEXT, kind TEXT NOT NULL DEFAULT 'audio', quality TEXT, audioTag TEXT, subTag TEXT, sizeBytes INTEGER NOT NULL DEFAULT 0, pinnedAt INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pins_videoKey ON pins (videoKey)")
    }
}

// v6 -> v7: feed cache (non-destructive; TTL auto-refreshes, safe to wipe anytime).
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS feed_cache (channelId TEXT NOT NULL, fetchedAt INTEGER NOT NULL DEFAULT 0, itemsJson TEXT NOT NULL DEFAULT '[]', PRIMARY KEY(channelId))")
    }
}

// v1 -> v2: playlist cover emoji (non-destructive: existing user data is preserved).
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE playlists ADD COLUMN emoji TEXT NOT NULL DEFAULT '🎵'")
    }
}

// ---- Mapping between player items and stored rows ----

fun PlaylistItemEntity.toVideoItem(): VideoItem = VideoItem(
    id = videoId,
    title = title,
    channel = channel,
    thumbnail = thumbnail,
    durationSec = durationSec,
    viewCount = viewCount,
    url = url
)

// Stable key for favorites/history/dedupe. Content-based fallback (title|channel|duration)
// instead of hashCode(): identical across rebuilds from the DB, so remove/toggle round-trips match.
// NOTE: keys minted by the old hashCode fallback will not match; only affects videos
// that had no id, no url and no title (degenerate rows).
// M2: escape helper for LIKE patterns (call site wraps with %...%).
fun escapeLike(s: String): String = s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

fun VideoItem.favKey(): String =
    id?.takeIf { it.isNotBlank() }
        ?: url?.takeIf { it.isNotBlank() }
        ?: listOfNotNull(
            title?.takeIf { it.isNotBlank() },
            channel?.takeIf { it.isNotBlank() },
            durationSec?.toString()
        ).joinToString("|").takeIf { it.isNotBlank() }
        ?: "untitled"

fun VideoItem.toFavorite(): FavoriteEntity = FavoriteEntity(
    videoKey = favKey(),
    videoId = id,
    title = title,
    channel = channel,
    thumbnail = thumbnail,
    url = url,
    durationSec = durationSec,
    viewCount = viewCount
)

fun VideoItem.toHistory(): HistoryEntity = HistoryEntity(
    videoKey = favKey(),
    videoId = id,
    title = title,
    channel = channel,
    thumbnail = thumbnail,
    url = url,
    durationSec = durationSec,
    viewCount = viewCount
)

fun HistoryEntity.toVideoItem(): VideoItem = VideoItem(
    id = videoId,
    title = title,
    channel = channel,
    thumbnail = thumbnail,
    durationSec = durationSec,
    viewCount = viewCount,
    url = url
)

fun FavoriteEntity.toVideoItem(): VideoItem = VideoItem(
    id = videoId,
    title = title,
    channel = channel,
    thumbnail = thumbnail,
    durationSec = durationSec,
    viewCount = viewCount,
    url = url
)

fun VideoItem.toEntity(pid: Long, pos: Int): PlaylistItemEntity = PlaylistItemEntity(
    playlistId = pid,
    videoId = id,
    title = title,
    channel = channel,
    thumbnail = thumbnail,
    url = url,
    durationSec = durationSec,
    viewCount = viewCount,
    position = pos
)

// Manual VideoItem JSON (feed cache only): 12 fields, no serializer dep.
object FeedJson {
    fun toJson(items: List<VideoItem>): String {
        val arr = org.json.JSONArray()
        for (v in items) {
            arr.put(org.json.JSONObject()
                .put("id", v.id ?: "").put("title", v.title ?: "").put("channel", v.channel ?: "")
                .put("thumbnail", v.thumbnail ?: "").put("durationSec", v.durationSec ?: -1)
                .put("viewCount", v.viewCount ?: -1).put("url", v.url ?: "")
                .put("channelAvatar", v.channelAvatar ?: "").put("verified", v.verified)
                .put("uploadDate", v.uploadDate ?: "").put("isShort", v.isShort)
                .put("channelUrl", v.channelUrl ?: ""))
        }
        return arr.toString()
    }
    fun fromJson(raw: String): List<VideoItem> {
        return try {
            val arr = org.json.JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                fun s(k: String): String? = o.optString(k, "").takeIf { it.isNotEmpty() }
                VideoItem(id = s("id"), title = s("title"), channel = s("channel"), thumbnail = s("thumbnail"),
                    durationSec = o.optLong("durationSec", -1).takeIf { it >= 0 },
                    viewCount = o.optLong("viewCount", -1).takeIf { it >= 0 }, url = s("url"),
                    channelAvatar = s("channelAvatar"), verified = o.optBoolean("verified", false),
                    uploadDate = s("uploadDate"), isShort = o.optBoolean("isShort", false),
                    channelUrl = s("channelUrl"))
            }
        } catch (_: Exception) { emptyList() }
    }
}

// v7 -> v8: channel groups (non-destructive; members CASCADE on group delete).
// v8 -> v9: playlist video mode (non-destructive; old rows stay "music").
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE playlists ADD COLUMN mode TEXT NOT NULL DEFAULT 'music'")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS channel_groups (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, createdAt INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE IF NOT EXISTS channel_group_members (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, groupId INTEGER NOT NULL, channelUrl TEXT NOT NULL, addedAt INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(groupId) REFERENCES channel_groups(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_channel_group_members_groupId ON channel_group_members (groupId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_channel_group_members_channelUrl ON channel_group_members (channelUrl)")
    }
}
