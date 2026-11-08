package com.mohamedadel.mytube.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
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
}

@Database(entities = [PlaylistEntity::class, PlaylistItemEntity::class, FavoriteEntity::class], version = 3, exportSchema = false)
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

// Stable key for favorites: video id when present, else the stream url.
fun VideoItem.favKey(): String = id?.takeIf { it.isNotBlank() } ?: url ?: title ?: hashCode().toString()

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
