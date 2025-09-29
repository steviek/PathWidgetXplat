package com.desaiwang.transit.path.api.alerts.everbridge

import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.RemoteFileRepository
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

object EverbridgeAlertsRepository {

    private val helper = RemoteFileRepository(
        keyPrefix = "everbridge_alerts",
        url = "https://panynj.gov/bin/portauthority/everbridge/incidents?status=All&department=Path",
        serializer = EverbridgeAlerts.serializer(),
        maxAge = 2.minutes
    )

    fun getAlerts(now: Instant): FetchWithPrevious<EverbridgeAlerts> {
        return helper.get(now)
    }
}
