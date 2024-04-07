package com.sixbynine.transit.path.api.alerts

import com.sixbynine.transit.path.api.NetworkException
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import com.sixbynine.transit.path.preferences.persistingInstant
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.JsonFormat
import com.sixbynine.transit.path.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object GithubAlertsRepository {
    private var storedAlerts by persisting(StringPreferencesKey("github_alerts"))
    private var storedAlertsTime by persistingInstant("github_alerts_time")

    private val httpClient = HttpClient()

    suspend fun getAlerts(): Result<GithubAlerts> = withContext(Dispatchers.IO) {
        suspendRunCatching {
            val storedAlertsTime = storedAlertsTime ?: return@suspendRunCatching
            if (storedAlertsTime < now() - 30.minutes) return@suspendRunCatching

            val storedAlertsJson = storedAlerts ?: return@suspendRunCatching
            val deserialized = JsonFormat.decodeFromString<GithubAlerts>(storedAlertsJson)
            return@withContext Result.success(deserialized)
        }

        suspendRunCatching {
            val response = withTimeout(5.seconds) {
                httpClient.get(
                    "https://raw.githubusercontent.com/steviek/PathWidgetXplat/main/alerts.json"
                )
            }

            if (!response.status.isSuccess()) {
                throw NetworkException(response.status.toString())
            }

            val responseText = response.bodyAsText()
            storedAlerts = responseText
            storedAlertsTime = now()
            JsonFormat.decodeFromString<GithubAlerts>(responseText)
        }
    }
}
