package com.desaiwang.transit.path.api.alerts.github

import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.RemoteFileRepository
import kotlinx.datetime.Instant
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
