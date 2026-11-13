package com.mohamedadel.mytube.ui.system

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.UpdateCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 2.3 slice 14 (2026-11-13): update check + diagnostics copy +
 * feed-worker scheduling extracted from the God-Activity. The report text
 * itself stays with the caller (it reads live player state).
 * Every function <= 30 lines.
 */
class SystemViewModel(app: Application) : AndroidViewModel(app) {

    private fun str(resId: Int): String =
        try { getApplication<Application>().getString(resId) } catch (_: Exception) { "" }

    private suspend fun toastMain(s: String) = withContext(Dispatchers.Main) {
        try { Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    private val _updateInfo = MutableStateFlow<UpdateCheck.Release?>(null)
    val updateInfo: StateFlow<UpdateCheck.Release?> = _updateInfo.asStateFlow()
    fun setUpdateInfo(v: UpdateCheck.Release?) { _updateInfo.value = v }

    fun checkUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val owner = str(R.string.update_repo_owner)
                val repo = str(R.string.update_repo_name)
                if (owner.isBlank() || repo.isBlank()) { toastMain(str(R.string.update_not_configured)); return@launch }
                toastMain(str(R.string.update_checking))
                val rel = UpdateCheck.latestRelease(owner, repo)
                if (rel == null) { toastMain(str(R.string.update_fail)); return@launch }
                val cur = com.mohamedadel.mytube.BuildConfig.VERSION_NAME
                if (!UpdateCheck.isNewer(rel.tag, cur)) toastMain(str(R.string.update_latest))
                else _updateInfo.value = rel
            } catch (_: Exception) { toastMain(str(R.string.update_fail)) }
        }
    }

    fun copyDiagnostics(report: String) {
        try {
            val ctx = getApplication<Application>()
            val cm = ctx.getSystemService(ClipboardManager::class.java) ?: return
            val clip = ClipData.newPlainText("MyTube diagnostics", report)
            try {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    clip.description.extras = android.os.PersistableBundle().apply {
                        putBoolean(android.content.ClipDescription.EXTRA_IS_SENSITIVE, true)
                    }
                }
            } catch (_: Exception) { }
            cm.setPrimaryClip(clip)
            viewModelScope.launch { toastMain(str(R.string.diag_copied)) }
        } catch (_: Exception) {
            viewModelScope.launch { toastMain(str(R.string.toast_delete_item_fail)) }
        }
    }

    fun syncFeedWorker(enable: Boolean) {
        try {
            com.mohamedadel.mytube.data.local.FeedRefreshWorker.schedule(getApplication(), enable)
        } catch (_: Exception) { }
    }
}
