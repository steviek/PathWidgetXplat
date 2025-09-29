package com.desaiwang.transit.path.native

import com.desaiwang.transit.path.Logging
import com.desaiwang.transit.path.NonFatalReporter
import com.desaiwang.transit.path.api.templine.HobClosureConfigRepository
import com.desaiwang.transit.path.widget.WidgetReloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NativeHolder {

    val widgetReloader = MutableStateFlow<WidgetReloader?>(null)

    fun initialize(
        widgetReloader: WidgetReloader,
        nonFatalReporter: (Throwable) -> Unit,
    ) {
        this.widgetReloader.value = widgetReloader

        // kick start some initialization here
        HobClosureConfigRepository.getConfig()

        Logging.nonFatalReporter = NonFatalReporter { e -> nonFatalReporter(e) }
    }
}

val widgetReloader: WidgetReloader
    get() = getInitialized(NativeHolder.widgetReloader)

private fun <T : Any> getInitialized(ref: StateFlow<T?>): T {
    return checkNotNull(ref.value) { "not initialized" }
}
