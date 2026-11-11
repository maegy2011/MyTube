package com.mohamedadel.mytube.data

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// G-A2 (audit P2 2026-11-11): sideloaded builds have no store loop — compare the installed
// versionName against the latest GitHub Release tag. Owner/repo come from string resources
// (update_repo_owner/_name); blank = feature dormant (toast, no network). Short timeouts:
// an update check must never hang Settings.
object UpdateCheck {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Deferred item 6 (2026-11-11): full release payload for the update dialog
    // (version + changelog body + download URL). Tag-only callers keep latestTag.
    data class Release(val tag: String, val body: String?, val htmlUrl: String?)
    fun latestRelease(owner: String, repo: String): Release? {
        return try {
            val req = Request.Builder()
                .url("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .build()
            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return null
                val body = r.body?.string() ?: return null
                val o = JSONObject(body)
                val tag = o.optString("tag_name", "").takeIf { it.isNotBlank() } ?: return null
                Release(tag, o.optString("body", "").takeIf { it.isNotBlank() }, o.optString("html_url", "").takeIf { it.isNotBlank() })
            }
        } catch (_: Exception) { null }
    }
    // latest release tag (e.g. "v0.3.2"), or null on any failure (offline/rate-limit/no release).
    fun latestTag(owner: String, repo: String): String? = latestRelease(owner, repo)?.tag

    // screens-fix Settings P0-S1: real numeric semver compare (was: any different tag = newer,
    // which also flagged older tags as "updates"). Numeric tie with differing text falls back
    // to a pre-release rule: bare release > suffixed pre-release, anything else = not newer.
    fun isNewer(latest: String, current: String): Boolean {
        return try {
            val l = latest.trim().trimStart('v', 'V')
            val c = current.trim().trimStart('v', 'V')
            if (l.isBlank() || c.isBlank() || l == c) return false
            fun nums(s: String): List<Int> {
                val core = Regex("^\\d+(\\.\\d+)*").find(s)?.value ?: return listOf(0)
                return core.split('.').map { it.toIntOrNull() ?: 0 }
            }
            val lp = nums(l)
            val cp = nums(c)
            for (i in 0 until maxOf(lp.size, cp.size)) {
                val a = lp.getOrElse(i) { 0 }
                val b = cp.getOrElse(i) { 0 }
                if (a != b) return a > b
            }
            fun tail(s: String): String {
                val core = Regex("^\\d+(\\.\\d+)*").find(s)?.value ?: ""
                return s.removePrefix(core)
            }
            tail(c).isNotEmpty() && tail(l).isEmpty()
        } catch (_: Exception) { false }
    }
}
