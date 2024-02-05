
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.sixbynine.transit.path.app.ui.home.HomeScreen
import com.sixbynine.transit.path.app.ui.settings.SettingScreen
import com.sixbynine.transit.path.app.ui.setup.SetupScreen
import com.sixbynine.transit.path.app.ui.theme.AppTheme
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()
        CompositionLocalProvider(LocalNavigator provides navigator) {
            AppTheme {
                NavHost(
                    modifier = Modifier
                        .background(colorScheme.background)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                        )
                        .fillMaxSize(),
                    navigator = navigator,
                    initialRoute = "/home"
                ) {
                    scene("/setup") {
                        SetupScreen()
                    }

                    scene("/home") {
                        HomeScreen()
                    }

                    scene("/settings") {
                        SettingScreen()
                    }
                }
            }
        }
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator not found") }
