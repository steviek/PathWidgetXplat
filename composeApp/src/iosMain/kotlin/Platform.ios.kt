import com.desaiwang.transit.path.time.IosPlatformTimeUtils
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val type = PlatformType.IOS

    fun setFirstDayOfWeek(firstDayOfWeek: String?) {
        IosPlatformTimeUtils.setFirstDayOfWeek(firstDayOfWeek)
    }
}

actual fun getPlatform(): Platform = IOSPlatform()

actual val IsDebug: Boolean = kotlin.native.Platform.isDebugBinary
