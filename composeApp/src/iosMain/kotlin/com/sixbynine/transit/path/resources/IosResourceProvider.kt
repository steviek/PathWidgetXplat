package com.sixbynine.transit.path.resources

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.time.now
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.edit_to_add_stations
import pathwidgetxplat.composeapp.generated.resources.error_long
import pathwidgetxplat.composeapp.generated.resources.error_short
import pathwidgetxplat.composeapp.generated.resources.updated_at_time

object IosResourceProvider {
    fun getEmptyStateString(): String = getStringBlocking(string.edit_to_add_stations)

    fun getUpdatedAtTime(formattedFetchTime: String): String {
        return getStringBlocking(string.updated_at_time, formattedFetchTime)
    }

    fun getErrorLong(): String = getStringBlocking(string.error_long)

    fun getErrorShort(): String = getStringBlocking(string.error_short)

    private inline fun getStringBlocking(resource: StringResource): String = runBlocking {
        val start = now()
        getString(resource).also { Logging.d("getString took ${now() - start}") }
    }

    private inline fun getStringBlocking(
        resource: StringResource,
        vararg formatArgs: Any
    ): String = runBlocking {
        getString(resource, *formatArgs)
    }
}
