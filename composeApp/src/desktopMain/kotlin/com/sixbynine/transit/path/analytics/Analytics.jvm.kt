package com.sixbynine.transit.path.analytics

actual fun AnalyticsStrategy(): AnalyticsStrategy = object : AnalyticsStrategy {
    override fun logEvent(
        name: String,
        params: Map<String, Any?>
    ) {
        println("Event: $name, Params: $params")
    }
}
