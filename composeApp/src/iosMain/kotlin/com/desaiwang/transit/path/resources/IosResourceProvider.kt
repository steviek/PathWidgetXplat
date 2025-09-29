package com.desaiwang.transit.path.resources

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.edit_to_add_stations
import pathwidgetxplat.composeapp.generated.resources.error_long
import pathwidgetxplat.composeapp.generated.resources.error_short
import pathwidgetxplat.composeapp.generated.resources.failed_to_fetch
import pathwidgetxplat.composeapp.generated.resources.failed_to_fetch_path_fault
import pathwidgetxplat.composeapp.generated.resources.station_empty
import pathwidgetxplat.composeapp.generated.resources.updated_at_time
import pathwidgetxplat.composeapp.generated.resources.updated_at_time_relative_full
import pathwidgetxplat.composeapp.generated.resources.updated_at_time_relative_shorter

object IosResourceProvider {
    fun getEmptyStateString(): String = getStringBlocking(string.edit_to_add_stations)

    fun getEmptyErrorMessage(isPathApiError: Boolean): String {
        val res = if (isPathApiError) string.failed_to_fetch_path_fault else string.failed_to_fetch
        return getStringBlocking(res)
    }

    fun getUpdatedAtTime(formattedFetchTime: String): String {
        return getStringBlocking(string.updated_at_time, formattedFetchTime)
    }

    fun getFullRelativeUpdatedAtTime(displayTime: String, dataTime: String): String {
        if (displayTime == dataTime) return getUpdatedAtTime(displayTime)
        return getStringBlocking(string.updated_at_time_relative_full, displayTime, dataTime)
    }

    fun getShorterRelativeUpdatedAtTime(displayTime: String, dataTime: String): String {
        if (displayTime == dataTime) return getUpdatedAtTime(displayTime)
        return getStringBlocking(string.updated_at_time_relative_shorter, displayTime, dataTime)
    }

    fun getErrorLong(): String = getStringBlocking(string.error_long)

    fun getErrorShort(): String = getStringBlocking(string.error_short)

    fun getNoTrainsText(): String = getStringBlocking(string.station_empty)

    private inline fun getStringBlocking(resource: StringResource): String = runBlocking {
        getString(resource)
    }

    private inline fun getStringBlocking(
        resource: StringResource,
        vararg formatArgs: Any
    ): String = runBlocking {
        getString(resource, *formatArgs)
    }
}
