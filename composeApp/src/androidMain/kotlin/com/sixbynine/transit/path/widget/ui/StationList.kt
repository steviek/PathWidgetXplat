package com.sixbynine.transit.path.widget.ui

import android.os.Build.VERSION
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter.Companion.tint
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.sixbynine.transit.path.MainActivity
import com.sixbynine.transit.path.R.drawable
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.secondOrNull
import com.sixbynine.transit.path.widget.WidgetData
import com.sixbynine.transit.path.widget.WidgetDataFormatter
import com.sixbynine.transit.path.widget.glance.GlanceTheme
import com.sixbynine.transit.path.widget.glance.Text

@Composable
fun DepartureBoard(result: DataResult<WidgetData>, modifier: GlanceModifier = GlanceModifier) {
    LazyColumn(modifier) {
        if (VERSION.SDK_INT >= 31) {
            item {
                Spacer(modifier = GlanceModifier.height(16.dp))
            }
        }

        result.data?.stations.orEmpty().forEachIndexed { index, station ->
            item {
                Column(
                    GlanceModifier
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(horizontal = 16.dp)
                ) {
                    if (index > 0) {
                        Spacer(GlanceModifier.height(8.dp))
                    }

                    Text(
                        text = station.displayName,
                        style = GlanceTheme.typography.header,
                        maxLines = 1,
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    station.signs.forEachIndexed { index, sign ->
                        Column {
                            if (index > 0) {
                                Spacer(GlanceModifier.height(4.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ColorBox(
                                    sign.colors.firstOrNull()?.toColorProvider(),
                                    sign.colors.secondOrNull()?.toColorProvider(),
                                )

                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Text(
                                    text = sign.title,
                                    style = GlanceTheme.typography.primary,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                            }

                            Row {
                                Spacer(modifier = GlanceModifier.width(26.dp))

                                Text(
                                    text = sign.projectedArrivals.joinToString(separator = "   ") {
                                        WidgetDataFormatter.formatTime(it)
                                    },
                                    style = GlanceTheme.typography.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(GlanceModifier.height(8.dp)) }
    }
}

private fun ColorWrapper.toColorProvider(): ColorProvider {
    return androidx.glance.color.ColorProvider(
        day = unwrap(isDark = false),
        night = unwrap(isDark = true)
    )
}

@Composable
private fun ColorBox(firstColor: ColorProvider?, secondColor: ColorProvider?) {
    Box(modifier = GlanceModifier.size(18.dp)) {
        Image(
            modifier = GlanceModifier.fillMaxSize(),
            provider = ImageProvider(drawable.circle),
            contentDescription = null,
            colorFilter = tint(firstColor ?: TransparentColorProvider)
        )

        Row(GlanceModifier.fillMaxSize()) {
            Spacer(GlanceModifier.defaultWeight())

            Image(
                modifier = GlanceModifier.defaultWeight(),
                provider = ImageProvider(drawable.circle_right),
                contentDescription = null,
                colorFilter = tint(secondColor ?: firstColor ?: TransparentColorProvider)
            )
        }

        Image(
            modifier = GlanceModifier.fillMaxSize(),
            provider = ImageProvider(drawable.circle_border),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    }
}

private val TransparentColorProvider = ColorProvider(Color.Transparent)
