package com.sixbynine.transit.path.time

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun is24HourClock(): Boolean {
    val dateFormat = NSDateFormatter.dateFormatFromTemplate(
        tmplate = "j",
        options = 0U,
        locale = NSLocale.currentLocale
    )
    return dateFormat?.indexOf("a")?.let { it < 0 } ?: true
}
