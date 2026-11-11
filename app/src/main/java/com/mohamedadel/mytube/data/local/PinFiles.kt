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
    // pin-runtime 2026-11-11: SD-first default centralized here (was duplicated in
    // MainActivity.requestPin without persisting, so the dialog showed "external"
    // while PinFiles.loc() still resolved "internal" on fresh installs).
    fun loc(context: Context): String {
        return try {
            val stored = context.getSharedPreferences("mytube_prefs", Context.MODE_PRIVATE).getString("pref_pin_location", null)
            stored ?: if (isExternalAvailable(context)) "external" else "internal"
        } catch (_: Exception) { "internal" }
    }

    fun baseDir(context: Context, location: String = loc(context)): File {
        if (location == "external" && isExternalAvailable(context)) {
            try {
                val ext = context.getExternalFilesDir(null)
                if (ext != null) return File(ext, DIR)
            } catch (_: Exception) { }
        }
        return File(context.filesDir, DIR)
    }

    // External app-specific dir needs NO storage permission (scoped storage),
    // but it can be missing (no SD/shared storage, unmounted, or OEM quirk).
    // Callers must fall back to internal rather than failing silently.
    fun isExternalAvailable(context: Context): Boolean {
        return try {
            if (android.os.Environment.getExternalStorageState() !=
                android.os.Environment.MEDIA_MOUNTED) return false
            val ext = context.getExternalFilesDir(null) ?: return false
            // probe writability once: mkdirs is cheap, a failed pin is not
            if (!ext.exists()) ext.mkdirs()
            ext.canWrite()
        } catch (_: Exception) { false }
    }

    // True when the current pref points somewhere unusable (user picked external
    // but it is unmounted/missing). UI surfaces this instead of a cryptic pin fail.
    fun isCurrentLocBroken(context: Context): Boolean =
        try { loc(context) == "external" && !isExternalAvailable(context) } catch (_: Exception) { false }

    fun sha(key: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            md.digest(key.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        } catch (_: Exception) { key.hashCode().toString() }
    }

    fun pinDir(context: Context, videoKey: String): File =
        File(baseDir(context), sha(videoKey))

    // All roots that can hold pins, regardless of the current pref (offline fix
    // 2026-11-11: pins stranded by a loc flip/unmounted SD must still resolve).
    fun roots(context: Context): List<File> {
        return try {
            val out = mutableListOf(File(context.filesDir, DIR))
            try {
                val ext = context.getExternalFilesDir(null)?.let { File(it, DIR) }
                if (ext != null && ext != out[0] && ext.exists()) out.add(ext)
            } catch (_: Exception) { }
            out
        } catch (_: Exception) { emptyList() }
    }

    // Whichever dir actually holds this pin: current loc first, then every known
    // root (internal + external). Pref flips and unmounted-SD fallbacks no longer orphan pins.
    fun existingDir(context: Context, videoKey: String): File? {
        return try {
            val h = sha(videoKey)
            val ordered = mutableListOf(pinDir(context, videoKey))
            for (r in roots(context)) {
                val d = File(r, h)
                if (ordered.none { it == d }) ordered.add(d)
            }
            ordered.firstOrNull { File(it, "meta.json").exists() }
        } catch (_: Exception) { null }
    }

    fun videoFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "video.mp4")
    fun audioFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "audio.m4a")
    fun subFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "sub.vtt")
    fun metaFile(context: Context, videoKey: String): File = File(pinDir(context, videoKey), "meta.json")

    // A pin is valid only when its descriptor exists (downloads are staged elsewhere, then moved in).
    fun isPinned(context: Context, videoKey: String): Boolean =
        try { existingDir(context, videoKey) != null } catch (_: Exception) { false }

    // Orphan scan: external dirs whose sha is NOT among the Room pin keys
    // (left behind after switching location to internal — invisible to delete/clearAll UI paths
    // that only enumerate the current base dir). Returns (count, bytes).
    fun externalOrphans(context: Context, knownKeys: List<String>): Pair<Int, Long> {
        return try {
            val ext = context.getExternalFilesDir(null) ?: return 0 to 0L
            val travel = java.io.File(ext, DIR)
            if (!travel.exists()) return 0 to 0L
            val known = knownKeys.map { sha(it) }.toSet()
            var n = 0; var b = 0L
            travel.listFiles()?.forEach { d ->
                try {
                    if (d.isDirectory && java.io.File(d, "meta.json").exists() && d.name !in known) {
                        n++
                        b += d.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                    }
                } catch (_: Exception) { }
            }
            n to b
        } catch (_: Exception) { 0 to 0L }
    }

    fun cleanExternalOrphans(context: Context, knownKeys: List<String>): Int {
        return try {
            val ext = context.getExternalFilesDir(null) ?: return 0
            val travel = java.io.File(ext, DIR)
            if (!travel.exists()) return 0
            val known = knownKeys.map { sha(it) }.toSet()
            var n = 0
            travel.listFiles()?.forEach { d ->
                try {
                    if (d.isDirectory && java.io.File(d, "meta.json").exists() && d.name !in known) {
                        d.deleteRecursively(); n++
                    }
                } catch (_: Exception) { }
            }
            n
        } catch (_: Exception) { 0 }
    }

    fun delete(context: Context, videoKey: String) {
        // offline fix 2026-11-11: clear EVERY root (a pin may live on the non-current one).
        try { pinDir(context, videoKey).deleteRecursively() } catch (_: Exception) { }
        try {
            val h = sha(videoKey)
            for (r in roots(context)) try { File(r, h).deleteRecursively() } catch (_: Exception) { }
        } catch (_: Exception) { }
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
            // existingDir: legacy internal pins report their real size even after the user switches loc
            val dir = existingDir(context, videoKey) ?: return 0L
            dir.walkTopDown()
                .filter { it.isFile }.sumOf { it.length() }
        } catch (_: Exception) { 0L }
    }

    fun totalBytes(context: Context): Long {
        return try {
            var total = 0L
            // sum BOTH roots: pins stay where they were downloaded when the user flips location
            val roots = listOfNotNull(
                File(context.filesDir, DIR).takeIf { it.exists() },
                try { context.getExternalFilesDir(null)?.let { File(it, DIR) }?.takeIf { it.exists() } } catch (_: Exception) { null }
            )
            for (root in roots) {
                try { total += root.walkTopDown().filter { it.isFile }.sumOf { it.length() } } catch (_: Exception) { }
            }
            total
        } catch (_: Exception) { 0L }
    }

    // Free bytes at the ACTUAL chosen pin root. -1 when unknown (caller must not block).
    // 2026-11-12 (runtime fix): baseDir() may not exist yet on fresh installs and
    // usableSpace on a missing path returns 0 — every pin then failed with a false
    // "no space" error though space exists. mkdirs first, then fall back to filesDir
    // and the external root before giving up with -1.
    fun freeBytes(context: Context, location: String = loc(context)): Long {
        return try {
            val base = try { baseDir(context, location) } catch (_: Exception) { null }
            if (base != null) {
                try { if (!base.exists()) base.mkdirs() } catch (_: Exception) { }
                if (base.exists()) {
                    val u = try { base.usableSpace } catch (_: Exception) { -1L }
                    if (u > 0) return u
                }
            }
            val internal = try {
                val f = context.filesDir
                try { if (!f.exists()) f.mkdirs() } catch (_: Exception) { }
                f.usableSpace
            } catch (_: Exception) { -1L }
            if (internal > 0) return internal
            val ext = try { context.getExternalFilesDir(null)?.usableSpace ?: -1L } catch (_: Exception) { -1L }
            if (ext > 0) return ext
            // 0 can still mean a truly full disk, but without a readable root we must
            // not block: -1 lets the guard skip instead of crying wolf.
            -1L
        } catch (_: Exception) { -1L }
    }
}
