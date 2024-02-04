package com.sixbynine.transit.path.app.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

abstract class BaseViewModel<State, Intent, Effect>(
    initialState: State
) : PathViewModel<State, Intent, Effect>() {

    private val _state = MutableStateFlow(initialState)
    final override val state = _state.asStateFlow()
    private val stateMutex = Mutex()

    private val _effects = Channel<Effect>()
    final override val effects = _effects.receiveAsFlow()

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch(Dispatchers.Default) { _effects.send(effect) }
    }

    protected fun updateState(block: State.() -> State) {
        _state.update(block)
    }
}
