package com.desaiwang.transit.path.widget

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build.VERSION
import android.view.Display
import android.widget.TextView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

/** Best guess at how wide [text] is when drawn with [size]. */
fun estimateTextWidth(context: Context, text: String, size: TextUnit): Dp {
    val textView = TextView(context.asUiContext())
    if (VERSION.SDK_INT >= 29) {
        textView.setTextAppearance(android.R.style.Theme_DeviceDefault_DayNight)
    }
    require(size.isSp)
    textView.textSize = size.value
    return context.pxToDp(textView.paint.measureText(text))
}

private fun Context.asUiContext(): Context {
    val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
    return createDisplayContext(defaultDisplay)
}

private fun Context.pxToDp(px: Float): Dp {
    val density = resources.displayMetrics.density
    return ceil(px / density).dp
}