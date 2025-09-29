package com.desaiwang.transit.path.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

actual fun AnalyticsStrategy(): AnalyticsStrategy = object : AnalyticsStrategy {
    override fun logEvent(name: String, params: Map<String, Any?>) {
        Firebase.analytics.logEvent(name) {
            params.forEach { (key, value) ->
                when (value) {
                    is Long -> param(key, value)
                    is String -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Boolean -> param(key, if (value) "true" else "false")
                }
            }
        }
    }
}
