package com.mohamedadel.mytube.data.youtube

import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as NPRequest
import org.schabi.newpipe.extractor.downloader.Response as NPResponse

class NewPipeDownloaderImpl : Downloader() {
    private val client = OkHttpClient()
    override fun execute(request: NPRequest): NPResponse {
        val builder = Request.Builder().url(request.url())
        request.headers().forEach { (k, v) -> v.forEach { builder.addHeader(k, it) } }
        request.dataToSend()?.let { builder.post(okhttp3.RequestBody.create(null, it)) } ?: builder.get()
        client.newCall(builder.build()).execute().use { r ->
            val body = r.body?.string() ?: ""
            val headers = mutableMapOf<String, List<String>>()
            r.headers.names().forEach { headers[it] = r.headers.values(it) }
            return NPResponse(r.code, r.message, headers, body, r.request.url.toString())
        }
    }
}
