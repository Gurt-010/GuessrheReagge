package com.soundstation.guessrhereagge.audio.youtube

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

class NewPipeDownloader private constructor() : Downloader() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override fun execute(request: Request): Response {
        val body = request.dataToSend()?.toRequestBody()
        val httpRequestBuilder = okhttp3.Request.Builder()
            .url(request.url())
            .method(request.httpMethod(), body)

        request.headers().forEach { (name, values) ->
            values.forEach { value -> httpRequestBuilder.addHeader(name, value) }
        }

        val httpResponse = client.newCall(httpRequestBuilder.build()).execute()
        val responseBody = httpResponse.body?.string().orEmpty()

        if (httpResponse.code == 429) {
            throw ReCaptchaException("recaptcha", request.url())
        }

        return Response(
            httpResponse.code,
            httpResponse.message,
            httpResponse.headers.toMultimap(),
            responseBody,
            httpResponse.request.url.toString(),
        )
    }

    companion object {
        private const val CONNECT_TIMEOUT_SEC = 20L
        private const val READ_TIMEOUT_SEC = 30L

        fun create(): NewPipeDownloader = NewPipeDownloader()
    }
}
