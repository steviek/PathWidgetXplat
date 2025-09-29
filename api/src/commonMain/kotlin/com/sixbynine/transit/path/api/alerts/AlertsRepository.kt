package com.desaiwang.transit.path.api.alerts

import com.desaiwang.transit.path.api.alerts.everbridge.EverbridgeAlertsRepository
import com.desaiwang.transit.path.api.alerts.everbridge.toCommonAlert
import com.desaiwang.transit.path.api.alerts.github.GithubAlertsRepository
import com.desaiwang.transit.path.util.FetchWithPrevious
import com.desaiwang.transit.path.util.combine
import kotlinx.datetime.Instant

object AlertsRepository {
    fun getAlerts(now: Instant): FetchWithPrevious<List<Alert>> {
        return combine(
            GithubAlertsRepository.getAlerts(now),
            EverbridgeAlertsRepository.getAlerts(now)
        ) { githubAlerts, everbridgeAlerts ->
            githubAlerts.alerts + everbridgeAlerts.data.map { it.toCommonAlert() }
        }
    }
}
