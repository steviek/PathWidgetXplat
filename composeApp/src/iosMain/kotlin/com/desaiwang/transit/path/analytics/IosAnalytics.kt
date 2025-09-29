package com.desaiwang.transit.path.analytics

import platform.Foundation.NSNotificationCenter

object IosAnalytics : AnalyticsStrategy {
    override fun logEvent(name: String, params: Map<String, Any?>) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            "logEvent",
            `object` = null,
            userInfo = params + ("event_name" to name)
        )
    }
}

actual fun AnalyticsStrategy(): AnalyticsStrategy = IosAnalytics
