package com.mohamedadel.mytube.data.youtube

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as NPRequest
import org.schabi.newpipe.extractor.downloader.Response as NPResponse

class NewPipeDownloaderImpl : Downloader() {
    // H2: same hung-thread risk as YouTubeRepo — extractor calls run on IO threads.
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(2, java.util.concurrent.TimeUnit.MINUTES)
            .build()
    }
    override fun execute(request: NPRequest): NPResponse {
        val builder = Request.Builder().url(request.url())
        request.headers().forEach { (k, v) -> v.forEach { builder.addHeader(k, it) } }
        request.dataToSend()?.let { builder.post(it.toRequestBody()) } ?: builder.get()
        client.newCall(builder.build()).execute().use { r ->
            val body = r.body?.string() ?: ""
            val headers = mutableMapOf<String, List<String>>()
            r.headers.names().forEach { headers[it] = r.headers.values(it) }
            return NPResponse(r.code, r.message, headers, body, r.request.url.toString())
        }
    }
}
