package com.sixbynine.transit.path.api.schedule

import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.RemoteFileRepository
import com.sixbynine.transit.path.util.combine
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

object GithubScheduleRepository {
    private val scheduleRepo = RemoteFileRepository(
        keyPrefix = "github_schedule",
        url = "https://raw.githubusercontent.com/steviek/PathWidgetXplat/main/schedule.json",
        serializer = Schedules.serializer(),
        maxAge = 3.days
    )

    private val scheduleOverrideRepo = RemoteFileRepository(
        keyPrefix = "github_schedule_override",
        url = "https://raw.githubusercontent.com/steviek/PathWidgetXplat/main/schedule_override.json",
        serializer = Schedules.serializer(),
        maxAge = 30.minutes
    )

    fun getSchedules(now: Instant): FetchWithPrevious<ScheduleAndOverride> {
        return combine(scheduleRepo.get(now), scheduleOverrideRepo.get(now)) { schedule, override ->
            ScheduleAndOverride(regularSchedule = schedule, override = override)
        }
    }
}
