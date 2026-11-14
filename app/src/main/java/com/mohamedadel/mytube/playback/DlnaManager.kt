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
                val svc = renderer.device.serviceList
                    .firstOrNull { it.serviceType.contains("AVTransport", ignoreCase = true) }
                    ?: return@launch
                svc.findAction("Stop")?.invokeSync(mapOf("InstanceID" to "0"))
            } catch (_: Exception) { }
        }
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
            "<res protocolInfo=\"http-get:*:$mime:*\">$u</res>" +
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
