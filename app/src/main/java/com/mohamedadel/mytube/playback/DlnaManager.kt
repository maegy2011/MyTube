package com.mohamedadel.mytube.playback

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.mm2d.upnp.ControlPoint
import net.mm2d.upnp.ControlPointFactory
import net.mm2d.upnp.Device

/**
 * DLNA/UPnP sender (2026-11-14, بأمر محمد).
 *
 * Discovers MediaRenderer devices (smart TVs, receivers) via SSDP and drives
 * them through AVTransport (SetAVTransportURI + Play). Playback URLs go
 * through [CastRelayServer] because renderers cannot fetch signed
 * `googlevideo.com` URLs directly (HTTP 403).
 *
 * Best-effort throughout: no renderers → empty list, no UI. Never throws.
 */
object DlnaManager {
    data class Renderer(val udn: String, val name: String, val device: Device)

    val renderers = mutableStateListOf<Renderer>()
    val available = mutableStateOf(false)
    var lastError: String? = null
        private set

    private var cp: ControlPoint? = null
    private var lock: WifiManager.MulticastLock? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val listener = object : ControlPoint.DiscoveryListener {
        override fun onDiscover(device: Device) {
            try {
                if (!device.deviceType.contains("MediaRenderer", ignoreCase = true)) return
                val udn = device.udn.ifBlank { device.ipAddress }
                scope.launch(Dispatchers.Main) {
                    if (renderers.none { it.udn == udn }) {
                        val nm = device.friendlyName.ifBlank { device.ipAddress }
                        renderers.add(Renderer(udn, nm, device))
                        available.value = true
                    }
                }
            } catch (_: Exception) { }
        }

        override fun onLost(device: Device) {
            try {
                val udn = device.udn.ifBlank { device.ipAddress }
                scope.launch(Dispatchers.Main) {
                    renderers.removeAll { it.udn == udn }
                    if (renderers.isEmpty()) available.value = false
                }
            } catch (_: Exception) { }
        }
    }

    /** Start discovery. Idempotent; safe to call on every resume. */
    fun start(app: Context) {
        try {
            if (cp != null) {
                scope.launch { runCatching { cp?.search(RENDERER_ST) } }
                return
            }
            acquireLock(app)
            val c = ControlPointFactory.create()
            c.initialize()
            c.addDiscoveryListener(listener)
            c.start()
            c.search(RENDERER_ST)
            cp = c
            lastError = null
        } catch (e: Exception) {
            lastError = e.javaClass.simpleName + ": " + (e.message ?: "")
        }
    }

    fun stop() {
        try { cp?.stop() } catch (_: Exception) { }
        try { cp?.terminate() } catch (_: Exception) { }
        cp = null
        releaseLock()
    }

    /**
     * Play [url] on [renderer] via AVTransport. Result to [done] on main.
     * Runs off the main thread. Returns via callback only.
     */
    fun play(renderer: Renderer, url: String, mime: String, title: String, done: (Boolean) -> Unit) {
        lastMedia = Triple(url, mime, title)
        scope.launch {
            val ok = try {
                val svc = renderer.device.serviceList
                    .firstOrNull { it.serviceType.contains("AVTransport", ignoreCase = true) }
                    ?: return@launch postDone(done, false)
                val set = svc.findAction("SetAVTransportURI") ?: return@launch postDone(done, false)
                set.invokeSync(mapOf("InstanceID" to "0", "CurrentURI" to url, "CurrentURIMetaData" to didlFor(url, mime, title)))
                val play = svc.findAction("Play") ?: return@launch postDone(done, false)
                play.invokeSync(mapOf("InstanceID" to "0", "Speed" to "1"))
                true
            } catch (_: Exception) {
                false
            }
            postDone(done, ok)
        }
    }

    fun stopPlayback(renderer: Renderer) {
        scope.launch {
            try {
                val svc = renderer.device.avt() ?: return@launch
                svc.findAction("Stop")?.invokeSync(mapOf("InstanceID" to "0"))
            } catch (_: Exception) { }
        }
    }

    /** Pause (keeps position). Result to [done] on main. */
    fun pause(renderer: Renderer, done: (Boolean) -> Unit) {
        scope.launch {
            val ok = try {
                val svc = renderer.device.avt() ?: return@launch postDone(done, false)
                svc.findAction("Pause")?.invokeSync(mapOf("InstanceID" to "0"))
                true
            } catch (_: Exception) {
                false
            }
            postDone(done, ok)
        }
    }

    /** Resume without re-setting the URI. Result to [done] on main. */
    fun resume(renderer: Renderer, done: (Boolean) -> Unit) {
        scope.launch {
            val ok = try {
                val svc = renderer.device.avt() ?: return@launch postDone(done, false)
                svc.findAction("Play")?.invokeSync(mapOf("InstanceID" to "0", "Speed" to "1"))
                true
            } catch (_: Exception) {
                false
            }
            postDone(done, ok)
        }
    }

    /** Seek to [ms]. Result to [done] on main. */
    fun seekTo(renderer: Renderer, ms: Long, done: (Boolean) -> Unit) {
        scope.launch {
            val ok = try {
                val svc = renderer.device.avt() ?: return@launch postDone(done, false)
                svc.findAction("Seek")?.invokeSync(
                    mapOf("InstanceID" to "0", "Unit" to "REL_TIME", "Target" to fmtTime(ms))
                )
                true
            } catch (_: Exception) {
                false
            }
            postDone(done, ok)
        }
    }

    /** Set volume 0..100. Result to [done] on main. */
    fun setVolume(renderer: Renderer, vol: Int, done: (Boolean) -> Unit) {
        scope.launch {
            val ok = try {
                val svc = renderer.device.rc() ?: return@launch postDone(done, false)
                svc.findAction("SetVolume")?.invokeSync(
                    mapOf("InstanceID" to "0", "Channel" to "Master", "DesiredVolume" to vol.coerceIn(0, 100).toString())
                )
                true
            } catch (_: Exception) {
                false
            }
            postDone(done, ok)
        }
    }

    /** Mute on/off. Result to [done] on main. */
    fun setMute(renderer: Renderer, mute: Boolean, done: (Boolean) -> Unit) {
        scope.launch {
            val ok = try {
                val svc = renderer.device.rc() ?: return@launch postDone(done, false)
                svc.findAction("SetMute")?.invokeSync(
                    mapOf("InstanceID" to "0", "Channel" to "Master", "DesiredMute" to if (mute) "1" else "0")
                )
                true
            } catch (_: Exception) {
                false
            }
            postDone(done, ok)
        }
    }

    /** Snapshot of remote state. Fields -1/unknown when unreadable. */
    data class RemoteState(val volume: Int, val mute: Boolean, val posMs: Long, val durMs: Long, val playing: Boolean)

    /** Last media handed to [play] — backs [replay] when the URI context is lost. */
    private var lastMedia: Triple<String, String, String>? = null

    /** Re-send the last media (SetURI+Play). Used when resume finds no URI. */
    fun replay(renderer: Renderer, done: (Boolean) -> Unit) {
        val m = lastMedia
        if (m == null) {
            scope.launch(Dispatchers.Main) { try { done(false) } catch (_: Exception) { } }
            return
        }
        play(renderer, m.first, m.second, m.third, done)
    }

    fun queryState(renderer: Renderer, done: (RemoteState) -> Unit) {
        scope.launch {
            // Parallel: 4 serial blocking SOAP calls made the dialog feel dead.
            val volD = kotlinx.coroutines.async {
                try {
                    renderer.device.rc()?.findAction("GetVolume")?.invokeSync(
                        mapOf("InstanceID" to "0", "Channel" to "Master")
                    )?.get("CurrentVolume")?.toIntOrNull() ?: -1
                } catch (_: Exception) { -1 }
            }
            val muteD = kotlinx.coroutines.async {
                try {
                    renderer.device.rc()?.findAction("GetMute")?.invokeSync(
                        mapOf("InstanceID" to "0", "Channel" to "Master")
                    )?.get("CurrentMute") == "1"
                } catch (_: Exception) { false }
            }
            val posD = kotlinx.coroutines.async {
                try {
                    renderer.device.avt()?.findAction("GetPositionInfo")
                        ?.invokeSync(mapOf("InstanceID" to "0"))
                        ?.let { parseTime(it["RelTime"]) to parseTime(it["TrackDuration"]) }
                } catch (_: Exception) { null }
            }
            val playD = kotlinx.coroutines.async {
                try {
                    renderer.device.avt()?.findAction("GetTransportInfo")
                        ?.invokeSync(mapOf("InstanceID" to "0"))
                        ?.get("CurrentTransportState")?.equals("PLAYING", ignoreCase = true) == true
                } catch (_: Exception) { false }
            }
            val pos = try { posD.await() } catch (_: Exception) { null }
            val st = RemoteState(
                try { volD.await() } catch (_: Exception) { -1 },
                try { muteD.await() } catch (_: Exception) { false },
                pos?.first ?: -1L, pos?.second ?: -1L,
                try { playD.await() } catch (_: Exception) { false }
            )
            launch(Dispatchers.Main) { try { done(st) } catch (_: Exception) { } }
        }
    }

    private fun Device.avt() = try {
        serviceList.firstOrNull { it.serviceType.contains("AVTransport", ignoreCase = true) }
    } catch (_: Exception) { null }

    private fun Device.rc() = try {
        serviceList.firstOrNull { it.serviceType.contains("RenderingControl", ignoreCase = true) }
    } catch (_: Exception) { null }

    internal fun fmtTime(ms: Long): String {
        // Locale.US: %d honors device locale — Arabic digits break renderers.
        val s = (ms.coerceAtLeast(0) / 1000).toInt()
        return String.format(java.util.Locale.US, "%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60)
    }

    internal fun parseTime(t: String?): Long {
        return try {
            val p = (t ?: "").split(":")
            if (p.size != 3) return -1
            ((p[0].toLong() * 3600 + p[1].toLong() * 60 + p[2].takeWhile { it.isDigit() }.toLong()) * 1000)
        } catch (_: Exception) { -1 }
    }

    private suspend fun postDone(done: (Boolean) -> Unit, ok: Boolean) {
        kotlinx.coroutines.withContext(Dispatchers.Main) { done(ok) }
    }

    private fun didlFor(url: String, mime: String, title: String): String {
        val t = title.ifBlank { "MyTube" }.escapeXml()
        val u = url.escapeXml()
        val cls = if (mime.startsWith("audio")) "object.item.audioItem.musicTrack" else "object.item.videoItem.movie"
        return "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
            "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">" +
            "<item id=\"0\" parentID=\"0\" restricted=\"1\">" +
            "<dc:title>$t</dc:title><upnp:class>$cls</upnp:class>" +
            // DLNA.ORG_OP=01: byte-seek supported (relay honors Range) — strict TVs need the flag.
            "<res protocolInfo=\"http-get:*:$mime:DLNA.ORG_OP=01;DLNA.ORG_CI=0\">$u</res>" +
            "</item></DIDL-Lite>"
    }

    private fun String.escapeXml(): String =
        replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&apos;")

    private fun acquireLock(app: Context) {
        try {
            val wm = app.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val l = wm?.createMulticastLock("mytube-dlna")
            l?.setReferenceCounted(true)
            try { l?.acquire() } catch (_: Exception) { }
            lock = l
        } catch (_: Exception) { }
    }

    private fun releaseLock() {
        try {
            val l = lock
            lock = null
            if (l?.isHeld == true) try { l.release() } catch (_: Exception) { }
        } catch (_: Exception) { }
    }

    private const val RENDERER_ST = "urn:schemas-upnp-org:device:MediaRenderer:1"
}
