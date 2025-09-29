package com.desaiwang.transit.path.api

import com.desaiwang.transit.path.flipper.FlipperUtil
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Interceptor

actual fun createHttpClient(
    block: HttpClientConfig<*>.() -> Unit
): HttpClient {
    return HttpClient(OkHttp) {
        block()

        engine {
            val debugInterceptor = FlipperUtil.interceptor()
            if (debugInterceptor is Interceptor) {
                addInterceptor(debugInterceptor)
            }
        }
    }
}
