package com.sixbynine.transit.path.api.schedule

import com.sixbynine.transit.path.schedule.Schedules

data class ScheduleAndOverride(
    val regularSchedule: Schedules,
    val override: Schedules?,
)
