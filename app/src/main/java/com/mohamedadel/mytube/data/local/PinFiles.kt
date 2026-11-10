package com.mohamedadel.mytube.data.local

import android.content.Context
import java.io.File
import java.security.MessageDigest

// Phase 2 — offline-pin file layout under filesDir/travel/<sha256(videoKey)>/.
// One pin = video.mp4 (muxed) OR video.mp4+audio.m4a (DASH pair) + optional sub.vtt + meta.json.
// The Room PinEntity row is the index (UI/badges/tabs read it, never the FS).
// Audio-only streaming default is untouched: pins capture whatever the user chose.
object PinFiles {
    const val DIR = "travel"

    // Download location pref ("internal" | "external"). External = app-specific dir
    // (no storage permission needed). New pins go to the current loc; old pins keep
    // working via existingDir()/isPinned() fallback to the legacy internal dir.
    fun loc(context: Context): String {
        return try { context.getSharedPreferences("mytube_prefs", Context.MODE_PRIVATE).getString("pref_pin_location", "internal") ?: "internal" } catch (_: Exception) { "internal" }
    }

    fun baseDir(context: Context, location: String = loc(context)): File {
        if (location == "external") {
            try {
                val ext = context.getExternalFilesDir(null)
                if (ext != null) return File(ext, DIR)
            } catch (_: Exception) { }
        }
        return File(context.filesDir, DIR)
    }

    fun sha(key: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            md.digest(key.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        } catch (_: Exception) { key.hashCode().toString() }
    }

    fun pinDir(context: Context, videoKey: String): File =
        File(baseDir(context), sha(videoKey))

    // Whichever dir actually holds this pin (current loc first, legacy internal fallback).
    fun existingDir(context: Context, videoKey: String): File? {
        return try {
            val cur = pinDir(context, videoKey)
            if (File(cur, "meta.json").exists()) cur
            else {
                val legacy = File(File(context.filesDir, DIR), sha(videoKey))
                if (File(legacy, "meta.json").exists()) legacy else null
            }
        } catch (_: Exception) { null }
    }

    fun videoFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "video.mp4")
    fun audioFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "audio.m4a")
    fun subFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "sub.vtt")
    fun metaFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "meta.json")

    // A pin is valid only when its descriptor exists (downloads are staged elsewhere, then moved in).
    fun isPinned(context: Context, videoKey: String): Boolean =
        try { existingDir(context, videoKey) != null } catch (_: Exception) { false }

    fun delete(context: Context, videoKey: String) {
        try { pinDir(context, videoKey).deleteRecursively() } catch (_: Exception) { }
        try { File(File(context.filesDir, DIR), sha(videoKey)).deleteRecursively() } catch (_: Exception) { }
    }

    fun clearAll(context: Context) {
        try { File(context.filesDir, DIR).deleteRecursively() } catch (_: Exception) { }
        try {
            val ext = context.getExternalFilesDir(null)
            if (ext != null) File(ext, DIR).deleteRecursively()
        } catch (_: Exception) { }
    }

    fun dirBytes(context: Context, videoKey: String): Long {
        return try {
            pinDir(context, videoKey).walkTopDown()
                .filter { it.isFile }.sumOf { it.length() }
        } catch (_: Exception) { 0L }
    }

    fun totalBytes(context: Context): Long {
        return try {
            val root = File(context.filesDir, DIR)
            if (!root.exists()) 0L
            else root.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        } catch (_: Exception) { 0L }
    }
}
