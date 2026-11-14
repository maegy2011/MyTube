package com.mohamedadel.mytube.playback

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.util.concurrent.atomic.AtomicReference

/**
 * Local relay for remote playback (2026-11-14).
 *
 * Why: YouTube `googlevideo.com` URLs are signed per-device and expire fast.
 * A Chromecast/DLNA receiver fetching them directly gets HTTP 403, so the
 * phone re-serves the bytes over LAN: receiver loads
 * `http://<phone-lan-ip>:<port>/play`, the relay forwards (with Range
 * passthrough for seeking) from the origin using a browser User-Agent.
 *
 * Lifecycle: [urlFor] (re)binds to the current snapshot; [stop] closes the
 * socket. Idempotent and best-effort — returns null when unusable and the
 * caller falls back to the direct URL.
 */
object CastRelayServer {
    private const val UA =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
    private var server: ServerSocket? = null
    private var worker: Thread? = null
    private val source = AtomicReference<Pair<String, String>?>(null)

    @Synchronized
    fun urlFor(url: String, mime: String): String? {
        if (url.isBlank()) return null
        return try {
            source.set(url to mime)
            val s = server?.takeIf { !it.isClosed } ?: bind()
            server = s
            val ip = lanIp() ?: return null
            "http://${ip}:${s.localPort}/play"
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun stop() {
        try { server?.close() } catch (_: Exception) { }
        server = null
        source.set(null)
    }

    private fun bind(): ServerSocket {
        val s = ServerSocket(0)
        val t = Thread({ acceptLoop(s) }, "cast-relay")
        t.isDaemon = true
        t.start()
        worker = t
        return s
    }

    private fun acceptLoop(s: ServerSocket) {
        while (!s.isClosed) {
            try {
                val c = s.accept()
                val t = Thread({ serve(c) }, "cast-relay-conn")
                t.isDaemon = true
                t.start()
            } catch (_: Exception) {
                break
            }
        }
    }

    private fun serve(c: Socket) {
        try {
            c.use { sock ->
                val src = source.get() ?: return
                val req = readHead(sock) ?: return
                val range = req.lines()
                    .firstOrNull { it.startsWith("Range:", ignoreCase = true) }
                    ?.substringAfter(":")?.trim()
                val conn = (URL(src.first).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", UA)
                    if (!range.isNullOrBlank()) setRequestProperty("Range", range)
                    instanceFollowRedirects = true
                }
                try {
                    val code = conn.responseCode
                    val out: OutputStream = sock.getOutputStream()
                    val len = conn.getHeaderField("Content-Length")
                    val ctype = conn.getHeaderField("Content-Type") ?: src.second
                    val crange = conn.getHeaderField("Content-Range")
                    writeLine(out, "HTTP/1.1 $code ${if (code == 206) "Partial Content" else "OK"}")
                    writeLine(out, "Content-Type: $ctype")
                    writeLine(out, "Accept-Ranges: bytes")
                    if (len != null) writeLine(out, "Content-Length: $len")
                    if (crange != null) writeLine(out, "Content-Range: $crange")
                    writeLine(out, "Connection: close")
                    writeLine(out, "")
                    val stream = try {
                        conn.inputStream
                    } catch (_: Exception) {
                        conn.errorStream ?: return
                    }
                    stream.use { it.copyTo(out) }
                    out.flush()
                } finally {
                    try { conn.disconnect() } catch (_: Exception) { }
                }
            }
        } catch (_: Exception) { }
    }

    private fun readHead(sock: Socket): String? {
        return try {
            sock.soTimeout = 10000
            val buf = ByteArray(8192)
            val n = sock.getInputStream().read(buf)
            if (n <= 0) null else String(buf, 0, n, Charsets.ISO_8859_1)
        } catch (_: Exception) {
            null
        }
    }

    private fun writeLine(out: OutputStream, s: String) {
        out.write((s + "\r\n").toByteArray(Charsets.ISO_8859_1))
    }

    private fun lanIp(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.toList()
                ?.flatMap { it.inetAddresses.toList() }
                ?.firstOrNull {
                    it is Inet4Address && !it.isLoopbackAddress && it.isSiteLocalAddress
                }?.hostAddress
        } catch (_: Exception) {
            null
        }
    }
}
