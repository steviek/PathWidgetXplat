package com.sixbynine.transit.path.api.alerts

import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawPathAlerts(
    @SerialName("ContentKey") val contentKey: String,
    @SerialName("Content") val content: String,
)

@Serializable
data class PathAlerts(val messages: List<PathAlert>)

@Serializable
data class PathAlert(
    val message: String,
    val isElevator: Boolean,
    val time: LocalTime?,
    val schedulesUrl: String?,
    val learnMoreUrl: String?,
)