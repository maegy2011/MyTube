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

    // latest release tag (e.g. "v0.3.2"), or null on any failure (offline/rate-limit/no release).
    fun latestTag(owner: String, repo: String): String? {
        return try {
            val req = Request.Builder()
                .url("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .build()
            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return null
                val body = r.body?.string() ?: return null
                JSONObject(body).optString("tag_name", "").takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) { null }
    }

    fun isNewer(latest: String, current: String): Boolean {
        return try {
            latest.trimStart('v', 'V') != current.trimStart('v', 'V')
        } catch (_: Exception) { false }
    }
}
