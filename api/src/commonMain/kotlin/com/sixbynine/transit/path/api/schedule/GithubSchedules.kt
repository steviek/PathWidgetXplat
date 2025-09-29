package com.desaiwang.transit.path.api.schedule

import com.desaiwang.transit.path.schedule.Schedules

data class ScheduleAndOverride(
    val regularSchedule: Schedules,
    val override: Schedules?,
)
