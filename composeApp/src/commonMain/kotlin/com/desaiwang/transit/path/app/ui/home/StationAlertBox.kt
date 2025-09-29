package com.desaiwang.transit.path.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.external.ExternalRoutingManager
import com.desaiwang.transit.path.app.ui.gutter
import com.desaiwang.transit.path.app.ui.icon.IconType
import com.desaiwang.transit.path.app.ui.icon.NativeIconButton
import com.desaiwang.transit.path.util.conditional
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.collapse
import pathwidgetxplat.composeapp.generated.resources.expand
import pathwidgetxplat.composeapp.generated.resources.open_in_browser

@Composable
fun AlertBox(
    text: String?,
    url: String?,
    colors: AlertBoxColors,
    isAlwaysExpanded: Boolean = false,
) {
    text ?: return
    var isExpanded by remember { mutableStateOf(isAlwaysExpanded) }

    Box(
        Modifier.padding(horizontal = gutter())
            .padding(bottom = 8.dp)
            .heightIn(if (isExpanded) 80.dp else 40.dp)
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.containerColor)
            .conditional(!isAlwaysExpanded) {
                clickable { isExpanded = !isExpanded }
            }
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = text,
                color = colors.textColor,
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(8.dp)
            )

            if (!isAlwaysExpanded) {
                val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)
                NativeIconButton(
                    icon = IconType.ExpandDown,
                    contentDescription = stringResource(
                        if (isExpanded) {
                            string.collapse
                        } else {
                            string.expand
                        }
                    ),
                    onClick = { isExpanded = !isExpanded },
                    buttonSize = 40.dp,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded && url != null,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            val scope = rememberCoroutineScope()
            NativeIconButton(
                icon = IconType.Internet,
                contentDescription = stringResource(string.open_in_browser),
                onClick = {
                    scope.launch {
                        ExternalRoutingManager().openUrl(url ?: return@launch)
                    }
                },
                buttonSize = 40.dp,
            )
        }
    }
}

data class AlertBoxColors(val containerColor: Color, val textColor: Color) {
    companion object {
        val Warning: AlertBoxColors
            @Composable
            get() = AlertBoxColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer
            )
        val Info: AlertBoxColors
            @Composable
            get() = AlertBoxColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
    }
}
