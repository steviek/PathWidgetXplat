package com.desaiwang.transit.path.api.path

import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.api.PathApiException
import com.desaiwang.transit.path.network.NetworkManager
import com.desaiwang.transit.path.preferences.StringPreferencesKey
import com.desaiwang.transit.path.preferences.persisting
import com.desaiwang.transit.path.preferences.persistingInstant
import com.desaiwang.transit.path.util.AgedValue
import com.desaiwang.transit.path.util.DataResult
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.IoScope
import com.desaiwang.transit.path.util.JsonFormat
import com.desaiwang.transit.path.util.Staleness
import com.desaiwang.transit.path.util.readRemoteFile
import com.desaiwang.transit.path.util.suspendRunCatching
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

object PathRepository {
    private var lastPathResponse by persisting(StringPreferencesKey("last_success"))
    private var lastPathResponseTime by persistingInstant("last_success_time")
    private var ongoingFetch: Deferred<DataResult<PathServiceResults>>? = null
    private val mutex = Mutex()

    fun getResults(now: Instant, staleness: Staleness): FetchWithPrevious<PathServiceResults> {
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

        return IoScope.async(start = LAZY) {
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
                            onSuccess = {
                                Logging.d("successfully fetched ridepath data")
                                DataResult.success(it)
                            },
                            onFailure = { error ->
                                val shouldBeConnected = NetworkManager().isConnectedToInternet()
                                var hadInternet = shouldBeConnected

                                if ("Unable to resolve host" in error.message.toString()) {
                                    hadInternet = false
                                }
                                Logging.w(
                                    "fetching ridepath failed, hadInternet=$hadInternet, shouldBeConnected=$shouldBeConnected",
                                    error
                                )
                                DataResult.failure(
                                    error,
                                    hadInternet = hadInternet,
                                    data = previous?.value
                                )
                            },
                        )
                        .also { ongoingFetch = null }
                }.also { ongoingFetch = it }
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
