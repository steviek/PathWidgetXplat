interface Platform {
    val name: String

    val type: PlatformType
}

expect fun getPlatform(): Platform

expect val IsDebug: Boolean

enum class PlatformType {
    ANDROID, IOS, DESKTOP
}