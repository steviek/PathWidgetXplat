package com.desaiwang.transit.path.app.ui

data class FontInfo(
    val size: Double,
    val isBold: Boolean = false,
    val isMonospacedDigit: Boolean = false
) {
    constructor(
        size: Int,
        isBold: Boolean = false,
        isMonospacedDigit: Boolean = false
    ) : this(size.toDouble(), isBold, isMonospacedDigit)
}
