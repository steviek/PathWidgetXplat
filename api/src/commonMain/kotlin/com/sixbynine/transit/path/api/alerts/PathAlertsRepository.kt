package com.sixbynine.transit.path.api.alerts

import com.sixbynine.transit.path.util.RemoteFileRepository
import kotlin.time.Duration.Companion.minutes

object PathAlertsRepository {

    private val MessageTextPattern = Regex(
        "\\\\u003Cspan\\s+class=&quotalertText[\\s%_a-z&]+\\\\u003E" +
                "([\\sa-zA-Z0-9.:/\\\\n\\-()'&]+)" +
                "\\\\u003C/span",
    )

    private val helper = RemoteFileRepository(
        keyPrefix = "path_alerts",
        url = "https://path-mppprod-app.azurewebsites.net/api/v1/AppContent/fetch?contentKey=PathAlert",
        remoteSerializer = RawPathAlerts.serializer(),
        localSerializer = PathAlerts.serializer(),
        maxAge = 10.minutes,
        remoteToLocal = this::parseAlerts
    )

    internal fun parseAlerts(rawPathAlerts: RawPathAlerts): PathAlerts {
        val alerts = MessageTextPattern.findAll(rawPathAlerts.content).mapNotNull {
            val message = it.groupValues.lastOrNull() ?: return@mapNotNull null
            parseAlert(message)
        }
        return PathAlerts(alerts.toList())
    }

    private fun parseAlert(message: String): PathAlert {
        return PathAlert(
            message = message,
            isElevator = message.contains("elevator", ignoreCase = true),
            time = null,
            schedulesUrl = null,
            learnMoreUrl = null,
        )
    }
}