package com.desaiwang.transit.path.test

import com.desaiwang.transit.path.util.TestRemoteFileProvider

object TestRemoteFileProviderImpl : TestRemoteFileProvider {
    override fun getText(url: String): Result<String> {
        val lastSlash = url.lastIndexOf('/')
        val path = url.substring(lastSlash + 1)
        val json = when (path) {
            "alerts.json" -> Alerts
            "schedule.json" -> Schedule
            "schedule_override.json" -> ScheduleOverride
            "ridepath.json" -> RidePath
            else -> return Result.failure(IllegalArgumentException("No test file for $path"))
        }
        return Result.success(json)
    }
}

fun TestRemoteFileProvider.Companion.install() {
    instance = TestRemoteFileProviderImpl
}
