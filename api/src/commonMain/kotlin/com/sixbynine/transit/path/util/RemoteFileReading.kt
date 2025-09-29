package com.desaiwang.transit.path.util

import com.desaiwang.transit.path.api.NetworkException
import com.desaiwang.transit.path.api.createHttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

private val httpClient by lazy { createHttpClient() }

suspend fun readRemoteFile(url: String): Result<String> = suspendRunCatching {
    TestRemoteFileProvider.instance
        ?.getText(url)
        ?.let { return@suspendRunCatching it.getOrThrow() }

    withContext(Dispatchers.IO) {
        val response = withTimeout(5.seconds) { httpClient.get(url) }

        if (!response.status.isSuccess()) {
            throw NetworkException(response.status.toString())
        }

        response.bodyAsText()
    }
}
