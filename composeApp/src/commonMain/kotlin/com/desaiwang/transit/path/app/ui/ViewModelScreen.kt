package com.desaiwang.transit.path.app.ui

import LocalNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import kotlinx.coroutines.flow.Flow
import moe.tlaster.precompose.navigation.Navigator

@Composable
fun <State : Any, Intent : Any, Effect : Any> ViewModelScreen(
    viewModelKey: String,
    createViewModel: () -> PathViewModel<State, Intent, Effect>,
    onEffect: suspend HandleEffectsScope.(Effect) -> Unit,
    content: @Composable ScreenScope<State, Intent>.() -> Unit,
) {
    val viewModel = getViewModel(
        key = viewModelKey,
        factory = viewModelFactory { createViewModel() }
    )
    HandleEffects(viewModel.effects) { effect ->
        onEffect(effect)
    }
    val state by viewModel.state.collectAsState()
    val scope = ScreenScope(state, viewModel::onIntent)
    scope.content()
}

@Composable
fun <T> HandleEffects(flow: Flow<T>, onEffect: suspend HandleEffectsScope.(T) -> Unit) {
    val scope = HandleEffectsScope(LocalNavigator.current, LocalUriHandler.current)
    LaunchedEffect(Unit) {
        flow.collect { scope.onEffect(it) }
    }
}

data class HandleEffectsScope(val navigator: Navigator, val uriHandler: UriHandler)

data class ScreenScope<State, Intent>(val state: State, val onIntent: (Intent) -> Unit)
