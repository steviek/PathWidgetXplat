package com.desaiwang.transit.path.api.alerts.github

import com.desaiwang.transit.path.api.alerts.Alert
import kotlinx.serialization.Serializable

@Serializable
data class GithubAlerts(val alerts: List<Alert>) {
    constructor(vararg alerts: Alert) : this(alerts.toList())
}
