package com.desaiwang.transit.path.util

import com.desaiwang.transit.path.preferences.StringPreferencesKey
import com.desaiwang.transit.path.time.now
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlin.time.Duration

class RemoteFileRepository<T>(
    keyPrefix: String,
    private val url: String,
    maxAge: Duration,
    private val serializer: KSerializer<T>
) {
    private var storedJson by persistingGlobally(StringPreferencesKey(keyPrefix))
    private var storedTime by persistingInstantGlobally("{$keyPrefix}_time")

    private val dataSource = DataSource(
        getCached = getCached@{
            val storedDataTime = storedTime ?: return@getCached null
            val storedDataJson = storedJson ?: return@getCached null
            val deserialized = JsonFormat.decodeFromString(serializer, storedDataJson)
            TimestampedValue(storedDataTime, deserialized)
        },
        fetch = {
            val responseText = readRemoteFile(url).getOrThrow()

            storedJson = responseText
            storedTime = now()
            JsonFormat.decodeFromString(serializer, responseText)
        },
        maxAge = maxAge
    )

    fun get(now: Instant): FetchWithPrevious<T> {
        return dataSource.get(now)
    }
}
