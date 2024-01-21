package com.sixbynine.transit.path.time

import android.text.format.DateFormat
import com.sixbynine.transit.path.PathApplication
import com.sixbynine.transit.path.PreviewContext

actual fun is24HourClock(): Boolean {
    return DateFormat.is24HourFormat(PreviewContext ?: PathApplication.instance)
}