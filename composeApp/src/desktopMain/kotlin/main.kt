
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sixbynine.transit.path.app.lifecycle.AppLifecycleObserver

fun main() = application {
    DisposableEffect(Unit) {
        AppLifecycleObserver.setAppIsActive(true)

        onDispose {
            AppLifecycleObserver.setAppIsActive(false)
        }
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Departures for PATH",
    ) {
        App()
    }
}
