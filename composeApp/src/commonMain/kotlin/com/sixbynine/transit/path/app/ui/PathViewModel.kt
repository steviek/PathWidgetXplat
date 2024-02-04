package com.sixbynine.transit.path.app.ui

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class PathViewModel<State, Intent, Effect> : ViewModel() {
    abstract val state: StateFlow<State>
    abstract fun onIntent(intent: Intent)
    abstract val effects: Flow<Effect>
}
