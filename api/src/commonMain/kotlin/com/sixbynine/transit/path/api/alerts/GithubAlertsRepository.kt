package com.sixbynine.transit.path.api.alerts

import com.sixbynine.transit.path.api.NetworkException
import com.sixbynine.transit.path.api.createHttpClient
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import com.sixbynine.transit.path.preferences.persistingInstant
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.util.AgedValue
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.JsonFormat
import com.sixbynine.transit.path.util.suspendRunCatching
import com.sixbynine.transit.path.util.toDataResult
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object GithubAlertsRepository {
    private var storedAlerts by persisting(StringPreferencesKey("github_alerts"))
    private var storedAlertsTime by persistingInstant("github_alerts_time")

    private val httpClient = createHttpClient()

    fun getAlerts(now: Instant): FetchWithPrevious<GithubAlerts> {
        val lastResult = runCatching {
            val storedAlertsTime = storedAlertsTime ?: return@runCatching null
            val storedAlertsJson = storedAlerts ?: return@runCatching null
            val deserialized = JsonFormat.decodeFromString<GithubAlerts>(storedAlertsJson)
            AgedValue(now - storedAlertsTime, deserialized)
        }.getOrNull()

        if (lastResult != null && lastResult.age < 30.minutes) {
            return FetchWithPrevious(
                previous = lastResult,
                fetch = { DataResult.success(lastResult.value) }
            )
        }

        val fetch = suspend {
            withContext(Dispatchers.IO) {
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
                    .toDataResult()
            }
        }
        return FetchWithPrevious(
            previous = lastResult,
            fetch = fetch
        )
    }
}
