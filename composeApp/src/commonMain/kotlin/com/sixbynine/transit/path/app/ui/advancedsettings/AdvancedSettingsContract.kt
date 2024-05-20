package com.sixbynine.transit.path.app.ui.advancedsettings

import com.sixbynine.transit.path.app.settings.AvoidMissingTrains
import com.sixbynine.transit.path.app.ui.ScreenScope
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.Intent
import com.sixbynine.transit.path.app.ui.advancedsettings.AdvancedSettingsContract.State

object AdvancedSettingsContract {
    data class State(
        val avoidMissingTrains: AvoidMissingTrains,
        val bottomSheet: BottomSheetType? = null,
    )

    enum class BottomSheetType {
        AvoidMissingTrains, CommutingSchedule
    }

    sealed interface Intent {
        data object BackClicked : Intent
        data class AvoidMissingTrainsChanged(val option: AvoidMissingTrains) : Intent
        data object AvoidMissingTrainsClicked : Intent
        data object CommutingScheduleClicked : Intent
        data object BottomSheetDismissed : Intent
    }

    sealed interface Effect {
        data object GoBack : Effect
    }
}

typealias AdvancedSettingsScope = ScreenScope<State, Intent>
