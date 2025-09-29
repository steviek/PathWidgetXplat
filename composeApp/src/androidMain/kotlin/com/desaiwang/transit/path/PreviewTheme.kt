package com.desaiwang.transit.path

import LocalNavigator
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.desaiwang.transit.path.app.ui.AppUiScope
import com.desaiwang.transit.path.app.ui.theme.AppTheme
import com.desaiwang.transit.path.app.ui.theme.Dimensions.isTablet
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.rememberNavigator

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, apiLevel = 33)
annotation class PathWidgetPreview

@SuppressLint("StaticFieldLeak") // Just for previews
var PreviewContext: Context? = null

@Composable
fun PreviewTheme(content: @Composable AppUiScope.() -> Unit) {
    PreviewContext = LocalContext.current
    val activity = getActivity()
    LaunchedEffect(Unit) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
    }

    PreComposeApp {
        val navigator = rememberNavigator()
        CompositionLocalProvider(
            LocalNavigator provides navigator,
        ) {
            AppTheme {
                BoxWithConstraints(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .fillMaxSize(),
                ) {
                    val scope = object : AppUiScope {
                        override val isTablet = isTablet()
                    }
                    scope.content()
                }
            }
        }
    }
}

@Composable
private fun getActivity(): ComponentActivity? {
    var context = LocalContext.current
    while (context is ContextWrapper || context is ComponentActivity) {
        if (context is ComponentActivity) return context
        check(context is ContextWrapper)
        context = context.baseContext
    }
    return null
}