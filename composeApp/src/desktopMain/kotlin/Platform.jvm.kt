
object JvmPlatform : Platform {
    override val name = "Java ${System.getProperty("java.version")}"
    override val type = PlatformType.DESKTOP
}

actual fun getPlatform(): Platform = JvmPlatform
actual val IsDebug: Boolean get() = true
