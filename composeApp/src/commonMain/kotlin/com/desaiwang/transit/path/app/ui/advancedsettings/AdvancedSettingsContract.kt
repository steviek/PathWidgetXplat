package com.desaiwang.transit.path.app.ui.advancedsettings

import com.desaiwang.transit.path.app.settings.AvoidMissingTrains
import com.desaiwang.transit.path.app.settings.CommutingConfiguration
import com.desaiwang.transit.path.app.settings.StationLimit
import com.desaiwang.transit.path.app.settings.TimeDisplay
import com.desaiwang.transit.path.app.ui.ScreenScope
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.State

object AdvancedSettingsContract {
    data class State(
        val avoidMissingTrains: AvoidMissingTrains,
        val timeDisplay: TimeDisplay,
        val groupTrains: Boolean,
        val stationLimit: StationLimit,
        val commutingConfiguration: CommutingConfiguration,
        val bottomSheet: BottomSheetType? = null,
    )

    enum class BottomSheetType {
        AvoidMissingTrains, CommutingSchedule, StationLimit, TimeDisplay
    }

    sealed interface Intent {
        data object BackClicked : Intent
        data class AvoidMissingTrainsChanged(val option: AvoidMissingTrains) : Intent
        data class CommutingConfigurationChanged(val configuration: CommutingConfiguration) : Intent
        data class TimeDisplayChanged(val display: TimeDisplay) : Intent
        data class StationLimitSelected(val limit: StationLimit) : Intent
        data object AvoidMissingTrainsClicked : Intent
        data object TimeDisplayClicked : Intent
        data class TrainGroupingClicked(val isEnabled: Boolean) : Intent
        data object CommutingScheduleClicked : Intent
        data object StationLimitClicked : Intent
        data object BottomSheetDismissed : Intent
    }

    sealed interface Effect {
        data object GoBack : Effect
    }
}

typealias AdvancedSettingsScope = ScreenScope<State, Intent>
