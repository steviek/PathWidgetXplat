package com.sixbynine.transit.path.api

import com.sixbynine.transit.path.api.impl.PathApiImpl
import com.sixbynine.transit.path.network.NetworkManager
import com.sixbynine.transit.path.util.AgedValue
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.FetchWithPrevious
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

interface PathApi {
    suspend fun fetchUpcomingDepartures(
        now: Instant,
        force: Boolean = false,
    ): Result<DepartureBoardTrainMap>

    fun getLastSuccessfulUpcomingDepartures(
        now: Instant,
    ): AgedValue<DepartureBoardTrainMap>?

    companion object {
        val instance: PathApi = PathApiImpl()
    }
}

fun PathApi.fetchUpcomingDeparturesWithPrevious(
    now: Instant,
    force: Boolean = false,
): FetchWithPrevious<DepartureBoardTrainMap> {
    val lastResult = getLastSuccessfulUpcomingDepartures(now)
    return FetchWithPrevious(
        previous = lastResult?.value?.takeUnless { lastResult.age > 2.minutes }
            ?.let { AgedValue(lastResult.age, it) },
        fetch = {
            fetchUpcomingDepartures(now, force)
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
                            data = lastResult?.value
                        )
                    },
                )
        }
    )
}
