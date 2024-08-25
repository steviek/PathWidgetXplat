package com.sixbynine.transit.path.api.path

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.api.PathApiException
import com.sixbynine.transit.path.api.createHttpClient
import com.sixbynine.transit.path.network.NetworkManager
import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import com.sixbynine.transit.path.preferences.persistingInstant
import com.sixbynine.transit.path.util.AgedValue
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.IoScope
import com.sixbynine.transit.path.util.JsonFormat
import com.sixbynine.transit.path.util.Staleness
import com.sixbynine.transit.path.util.readRemoteFile
import com.sixbynine.transit.path.util.suspendRunCatching
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

object PathRepository {
    private val httpClient = createHttpClient()
    private var lastPathResponse by persisting(StringPreferencesKey("last_success"))
    private var lastPathResponseTime by persistingInstant("last_success_time")
    private var ongoingFetch: Deferred<DataResult<PathServiceResults>>? = null
    private val mutex = Mutex()

    fun getResults(now: Instant, staleness: Staleness): FetchWithPrevious<PathServiceResults> {
        Logging.d("getResults, staleAfter: ${staleness.staleAfter}, invalidAfter: ${staleness.invalidAfter}")
        val previous = getCachedResults(now)
        return FetchWithPrevious.create(
            previous = previous,
            fetch = { fetch(now, previous) },
            staleness = staleness,
        )
    }

    private fun fetch(
        now: Instant,
        previous: AgedValue<PathServiceResults>?
    ): Deferred<DataResult<PathServiceResults>> {
        ongoingFetch?.takeIf { it.isActive }?.let {
            Logging.d("Join existing fetch")
            return it
        }

        return IoScope.async {
            val fetch = mutex.withLock {
                ongoingFetch?.takeIf { it.isActive }?.let {
                    Logging.d("Join existing fetch")
                    return@withLock it
                }


                Logging.d("Starting a new fetch, previous had age of ${previous?.age}")

                async {
                    suspendRunCatching {
                        withTimeout(5.seconds) {
                            val responseText =
                                readRemoteFile(
                                    "https://www.panynj.gov/bin/portauthority/ridepath.json",
                                )
                                    .getOrThrow()

                            lastPathResponseTime = now
                            lastPathResponse = responseText

                            JsonFormat.decodeFromString<PathServiceResults>(responseText).also {
                                if (it.results.isEmpty()) {
                                    throw PathApiException.NoResults
                                }
                            }
                        }
                    }
                        .fold(
                            onSuccess = { DataResult.success(it) },
                            onFailure = { error ->
                                var hadInternet = NetworkManager().isConnectedToInternet()

                                if ("Unable to resolve host" in error.message.toString()) {
                                    hadInternet = false
                                }
                                DataResult.failure(
                                    error,
                                    hadInternet = hadInternet,
                                    data = previous?.value
                                )
                            },
                        )
                }
            }

            fetch.await()
        }
    }

    private fun getCachedResults(now: Instant): AgedValue<PathServiceResults>? {
        val lastPathResponseTime = lastPathResponseTime ?: return null
        val result = runCatching {
            val lastPathResponse = lastPathResponse ?: return null
            JsonFormat.decodeFromString<PathServiceResults>(lastPathResponse)
        }

        return result.getOrNull()
            ?.takeIf { it.results.isNotEmpty() }
            ?.let { AgedValue(now - lastPathResponseTime, it) }
    }

    @Serializable
    data class PathServiceResults(val results: List<PathServiceResult>)

    @Serializable
    data class PathServiceResult(
        val consideredStation: String,
        val destinations: List<PathDestination>
    )

    @Serializable
    data class PathDestination(
        val label: String,
        val messages: List<PathDestinationMessage>
    )

    @Serializable
    data class PathDestinationMessage(
        val target: String,
        val secondsToArrival: String,
        val arrivalTimeMessage: String?,
        val lineColor: String,
        val headSign: String,
        val lastUpdated: Instant
    ) {
        val durationToArrival get() = secondsToArrival.toInt().seconds
    }
}
