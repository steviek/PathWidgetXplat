package com.sixbynine.transit.path.api.alerts.github

import com.sixbynine.transit.path.util.FetchWithPrevious
import com.sixbynine.transit.path.util.RemoteFileRepository
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes

object GithubAlertsRepository {

    private val helper = RemoteFileRepository(
        keyPrefix = "github_alerts",
        url = "https://raw.githubusercontent.com/steviek/PathWidgetXplat/main/alerts.json",
        serializer = GithubAlerts.serializer(),
        maxAge = 30.minutes
    )

    fun getAlerts(now: Instant): FetchWithPrevious<GithubAlerts> {
        return helper.get(now)
    }
}
