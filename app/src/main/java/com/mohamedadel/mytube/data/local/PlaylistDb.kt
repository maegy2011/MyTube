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
    val createdAt: Long = System.currentTimeMillis()
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

// Playlist row + item count for the management screen.
data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val emoji: String,
    val createdAt: Long,
    val count: Int
)

@Dao
interface PlaylistDao {
    @Query("SELECT p.id AS id, p.name AS name, p.emoji AS emoji, p.createdAt AS createdAt, COUNT(i.id) AS count FROM playlists p LEFT JOIN playlist_items i ON i.playlistId = p.id GROUP BY p.id ORDER BY p.createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :pid ORDER BY position ASC")
    fun observeItems(pid: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM playlist_items WHERE playlistId = :pid ORDER BY position ASC")
    suspend fun itemsOnce(pid: Long): List<PlaylistItemEntity>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :pid")
    suspend fun itemCount(pid: Long): Int

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
    @Transaction
    suspend fun savePlaylistEdits(pid: Long, name: String, emoji: String, orderedIds: List<Long>) {
        if (name.isNotBlank()) rename(pid, name)
        setEmoji(pid, emoji.ifBlank { "🎵" })
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

    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY playedAt DESC LIMIT 100)")
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
}

@Database(entities = [PlaylistEntity::class, PlaylistItemEntity::class, FavoriteEntity::class, HistoryEntity::class, SubscriptionEntity::class], version = 5, exportSchema = false)
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
