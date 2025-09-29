import PlatformType.ANDROID
import android.os.Build
import com.desaiwang.transit.path.BuildConfig

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val type = ANDROID
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val IsDebug: Boolean = BuildConfig.DEBUG
