package com.sixbynine.transit.path.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> HandleEffects(effects: Flow<T>, onEffect: (T) -> Unit) {
    LaunchedEffect(Unit) {
        effects.collect { onEffect(it) }
    }
}
