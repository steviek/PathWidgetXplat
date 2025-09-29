import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.desaiwang.transit.path.app.ui.advancedsettings.AdvancedSettingsScreen
import com.desaiwang.transit.path.app.ui.home.HomeScreen
import com.desaiwang.transit.path.app.ui.settings.SettingScreen
import com.desaiwang.transit.path.app.ui.setup.SetupScreen
import com.desaiwang.transit.path.app.ui.station.StationScreen
import com.desaiwang.transit.path.app.ui.theme.AppTheme
import com.desaiwang.transit.path.app.ui.theme.Dimensions.isTablet
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()
        BoxWithConstraints {
            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalIsTablet provides isTablet()
            ) {
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

                        scene("/station/{id}") { backStackEntry ->
                            val stationId: String? = backStackEntry.path<String>("id")
                            StationScreen(stationId)
                        }

                        scene("/settings") {
                            SettingScreen()
                        }

                        scene("/advanced_settings") {
                            AdvancedSettingsScreen()
                        }
                    }
                }
            }
        }
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator not found") }
val LocalIsTablet = compositionLocalOf { false }
