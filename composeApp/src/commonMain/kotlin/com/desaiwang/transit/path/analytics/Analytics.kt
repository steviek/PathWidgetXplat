package com.desaiwang.transit.path.analytics

import com.desaiwang.transit.path.api.Line
import com.desaiwang.transit.path.api.LocationSetting
import com.desaiwang.transit.path.api.Station
import com.desaiwang.transit.path.api.StationSort
import com.desaiwang.transit.path.api.TrainFilter
import com.desaiwang.transit.path.app.settings.AvoidMissingTrains
import com.desaiwang.transit.path.app.settings.StationLimit
import com.desaiwang.transit.path.app.settings.TimeDisplay

object Analytics {
    private val strategy = AnalyticsStrategy()

    fun appLaunched() {
        strategy.logEvent("app_launched")
    }

    fun stationAdded(station: Station) {
        strategy.logEvent("add_station", mapOf("station" to station.pathApiName))
    }

    fun stationRemoved(station: Station) {
        strategy.logEvent("remove_station", mapOf("station" to station.pathApiName))
    }

    fun timeDisplaySet(timeDisplay: TimeDisplay) {
        strategy.logEvent("set_time_display", mapOf("time_display" to timeDisplay.name.lowercase()))
    }

    fun locationSettingSet(setting: LocationSetting) {
        strategy.logEvent("set_location_setting", mapOf("setting" to setting.name.lowercase()))
    }

    fun filterSet(filter: TrainFilter) {
        strategy.logEvent("set_filter", mapOf("filter" to filter.name.lowercase()))
    }

    fun lineFiltersSet(filters: Set<Line>) {
        strategy.logEvent(
            "set_line_filter",
            mapOf("filters" to filters.joinToString { it.name.lowercase() })
        )
    }

    fun stationSortSet(sort: StationSort) {
        strategy.logEvent("set_station_order", mapOf("order" to sort.name.lowercase()))
    }

    fun stationLimitSet(limit: StationLimit) {
        strategy.logEvent("set_station_limit", mapOf("limit" to limit.name.lowercase()))
    }

    fun avoidMissingTrainsSet(option: AvoidMissingTrains) {
        strategy.logEvent("avoid_missing_trains_set", mapOf("option" to option.name.lowercase()))
    }

    fun commutingConfigurationSet() {
        strategy.logEvent("avoid_missing_trains_set")
    }

    fun shareAppClicked() {
        strategy.logEvent("share_app_clicked")
    }

    fun rateAppClicked() {
        strategy.logEvent("rate_app_clicked")
    }

    fun iosStoreReviewControllerIssue(deviceName: String, iosVersion: String) {
        strategy.logEvent(
            "ios_store_review_controller_issue",
            mapOf("device_name" to deviceName, "ios_version" to iosVersion)
        )
    }

    fun buyMeACoffeeClicked() {
        strategy.logEvent("buy_me_a_coffee_clicked")
    }
}

interface AnalyticsStrategy {
    fun logEvent(name: String, params: Map<String, Any?> = mapOf())
}

expect fun AnalyticsStrategy(): AnalyticsStrategy
