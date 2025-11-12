package com.sixbynine.transit.path.native

import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.NonFatalReporter
import com.sixbynine.transit.path.widget.WidgetReloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NativeHolder {

    val widgetReloader = MutableStateFlow<WidgetReloader?>(null)

    fun initialize(
        widgetReloader: WidgetReloader,
        nonFatalReporter: (Throwable) -> Unit,
    ) {
        this.widgetReloader.value = widgetReloader

        Logging.nonFatalReporter = NonFatalReporter { e -> nonFatalReporter(e) }
    }
}

val widgetReloader: WidgetReloader
    get() = getInitialized(NativeHolder.widgetReloader)

private fun <T : Any> getInitialized(ref: StateFlow<T?>): T {
    return checkNotNull(ref.value) { "not initialized" }
}
