package com.desaiwang.transit.path.app.ui.setup

import com.desaiwang.transit.path.app.ui.setup.SetupScreenContract.Effect
import com.desaiwang.transit.path.app.ui.setup.SetupScreenContract.Intent.ConfirmClicked
import com.desaiwang.transit.path.app.ui.setup.SetupScreenContract.Intent.StationCheckedChanged
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(SetupScreenContract.State())
    val state = _state.asStateFlow()

    private val _effects = Channel<Effect>()
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: SetupScreenContract.Intent) {
        when (intent) {
            is StationCheckedChanged -> {
                _state.update {
                    it.copy(
                        selectedStations = if (intent.isChecked) {
                            it.selectedStations + intent.station
                        } else {
                            it.selectedStations - intent.station
                        }
                    )
                }
            }

            ConfirmClicked -> {
                viewModelScope.launch {
                    _effects.send(Effect.NavigateToHome)
                }
            }
        }
    }
}