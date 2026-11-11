// On-device audio (MediaStore): plays files already on the device, no network, no account.
// Two scan modes: strict (IS_MUSIC = songs only) and compat (every audio entry with a
// sane duration — for devices where MediaStore misses things). Folder exclusion is a
// plain prefs string-set of bucket names.
package com.mohamedadel.mytube.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.mohamedadel.mytube.data.youtube.VideoItem

data class LocalTrack(
    val id: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationSec: Long?,
    val uri: String,
    val folder: String?
)

fun LocalTrack.toVideoItem(): VideoItem = VideoItem(
    id = "local:$id",
    title = title,
    channel = artist,
    thumbnail = null,
    durationSec = durationSec,
    viewCount = null,
    url = uri
)

fun String.isLocalAudioUri(): Boolean =
    startsWith("content://media/") || startsWith("file://")

// L9 (audit P2 2026-11-11): explicit type prefix + check before play. Local tracks are
// minted with id="local:<mediaId>" (see toVideoItem); YouTube keys keep their bare form
// deliberately — prefixing them yt: would orphan every existing favorite/history row.
fun VideoItem.isLocal(): Boolean =
    id?.startsWith("local:") == true || url?.isLocalAudioUri() == true

fun Context.scanLocalAudio(compat: Boolean): List<LocalTrack> {
    val out = mutableListOf<LocalTrack>()
    try {
        val coll = if (Build.VERSION.SDK_INT >= 29) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
        )
        // compat mode drops the IS_MUSIC filter and keeps anything longer than 30s
        // (ringtones/notifications/voice notes usually fall below it)
        val sel = if (compat) "${MediaStore.Audio.Media.DURATION} > ?" else
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > ?"
        val args = arrayOf("30000")
        val sort = "${MediaStore.Audio.Media.TITLE} ASC"
        try {
            contentResolver.query(coll, proj, sel, args, sort)?.use { c ->
                val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val iTitle = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val iArtist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val iBucket = try { c.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME) } catch (_: Exception) { -1 }
                while (c.moveToNext()) {
                    try {
                        val id = c.getLong(iId)
                        val dur = try { c.getLong(iDur) / 1000L } catch (_: Exception) { null }
                        out.add(
                            LocalTrack(
                                id = id,
                                title = try { c.getString(iTitle) } catch (_: Exception) { null },
                                artist = try { c.getString(iArtist) } catch (_: Exception) { null },
                                album = try { c.getString(iAlbum) } catch (_: Exception) { null },
                                durationSec = dur?.takeIf { it > 0 },
                                uri = Uri.withAppendedPath(coll, id.toString()).toString(),
                                folder = if (iBucket >= 0) try { c.getString(iBucket) } catch (_: Exception) { null } else null
                            )
                        )
                    } catch (_: Exception) { }
                }
            }
        } catch (_: Exception) { }
    } catch (_: Exception) { }
    return out
}
