package com.sixbynine.transit.path.api.alerts

import com.sixbynine.transit.path.api.NetworkException
import com.sixbynine.transit.path.preferences.LongPreferencesKey
import com.sixbynine.transit.path.preferences.Preferences
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.JsonFormat
import com.sixbynine.transit.path.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

object GithubAlertsRepository {
    private val preferences = Preferences()
    private val storedAlertsKey = StringPreferencesKey("github_alerts")
    private val storedAlertsTimeKey = LongPreferencesKey("github_alerts_time")

    private val httpClient = HttpClient()

    suspend fun getAlerts(): Result<GithubAlerts> {
        suspendRunCatching {
            val storedAlertsTimeMillis =
                preferences[storedAlertsTimeKey] ?: return@suspendRunCatching
            val storedAlertsTime = Instant.fromEpochMilliseconds(storedAlertsTimeMillis)
            if (storedAlertsTime < now() - 30.minutes) return@suspendRunCatching

            val storedAlertsJson = preferences[storedAlertsKey] ?: return@suspendRunCatching
            val deserialized = JsonFormat.decodeFromString<GithubAlerts>(storedAlertsJson)
            return Result.success(deserialized)
        }

        return suspendRunCatching {
            val response =
                httpClient.get(
                    "https://raw.githubusercontent.com/steviek/PathWidgetXplat/main/alerts.json"
                )
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                preferences[storedAlertsKey] = responseText
                preferences[storedAlertsTimeKey] = now().toEpochMilliseconds()
                JsonFormat.decodeFromString<GithubAlerts>(responseText)
            } else {
                throw NetworkException(response.status.toString())
            }
        }
    }
}
