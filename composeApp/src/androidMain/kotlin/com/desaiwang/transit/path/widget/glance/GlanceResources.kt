package com.desaiwang.transit.path.widget.glance

import androidx.compose.runtime.Composable
import androidx.glance.GlanceComposable
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

// Glance runs on a background thread, so blocking is ok here.
@GlanceComposable
@Composable
fun stringResource(resource: StringResource): String {
    return runBlocking { getString(resource) }
}

@GlanceComposable
@Composable
fun stringResource(resource: StringResource, vararg args: Any): String {
    return runBlocking { getString(resource, *args) }
}