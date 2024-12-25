package com.sixbynine.transit.path.util

import com.sixbynine.transit.path.preferences.StringPreferencesKey
import com.sixbynine.transit.path.preferences.persisting
import com.sixbynine.transit.path.preferences.persistingInstant
import com.sixbynine.transit.path.time.now
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlin.time.Duration

class RemoteFileRepository<Remote, Local>(
    keyPrefix: String,
    private val url: String,
    maxAge: Duration,
    private val remoteSerializer: KSerializer<Remote>,
    private val localSerializer: KSerializer<Local>,
    private val remoteToLocal: (Remote) -> Local,

) {
    private var storedJson by persisting(StringPreferencesKey(keyPrefix))
    private var storedTime by persistingInstant("{$keyPrefix}_time")

    private val dataSource = DataSource<Local>(
        getCached = getCached@{
            val storedDataTime = storedTime ?: return@getCached null
            val storedDataJson = storedJson ?: return@getCached null
            val deserialized = JsonFormat.decodeFromString(localSerializer, storedDataJson)
            TimestampedValue(storedDataTime, deserialized)
        },
        fetch = {
            val responseText = readRemoteFile(url).getOrThrow()
            val remote = JsonFormat.decodeFromString(remoteSerializer, responseText)
            val local = remoteToLocal(remote)

            storedJson = if (local == remote) {
                responseText
            } else {
                JsonFormat.encodeToString(localSerializer, local)
            }
            storedTime = now()

            local
        },
        maxAge = maxAge
    )

    fun get(now: Instant): FetchWithPrevious<Local> {
        return dataSource.get(now)
    }
}

fun <T> RemoteFileRepository(
    keyPrefix: String,
    url: String,
    maxAge: Duration,
    serializer: KSerializer<T>,
): RemoteFileRepository<T, T> {
    return RemoteFileRepository(
        keyPrefix,
        url,
        maxAge,
        serializer,
        serializer,
        { it }
    )
}
