package com.sixbynine.transit.path.time

import android.text.format.DateFormat
import com.sixbynine.transit.path.PathApplication

actual fun is24HourClock(): Boolean {
    return DateFormat.is24HourFormat(PathApplication.instance)
}