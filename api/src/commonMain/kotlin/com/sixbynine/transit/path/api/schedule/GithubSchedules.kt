package com.sixbynine.transit.path.api.schedule

import com.sixbynine.transit.path.schedule.Timetables

data class ScheduleAndOverride(
    val regularSchedule: Timetables,
    val override: Timetables?,
)
