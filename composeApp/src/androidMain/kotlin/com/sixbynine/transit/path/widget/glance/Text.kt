package com.sixbynine.transit.path.widget.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.glance.GlanceModifier
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextStyle

@Composable
fun Text(
    text: String,
    modifier: GlanceModifier = GlanceModifier,
    style: TextStyle = TextDefaults.defaultTextStyle,
    maxLines: Int = Int.MAX_VALUE,
    fontSize: TextUnit? = null,
    textAlign: TextAlign? = null,
) {
    androidx.glance.text.Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            fontSize = fontSize ?: style.fontSize,
            textAlign = textAlign ?: style.textAlign,
        ),
        maxLines = maxLines,
    )
}