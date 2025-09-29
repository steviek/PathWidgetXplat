# Departures App for PATH

This is a [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) project targeting Android and iOS.

The App UI is written in [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) with minimal platform-specific code.

The widgets are native by necessity, and written in [SwiftUI](https://developer.apple.com/documentation/widgetkit/swiftui-views) on iOS and [Jetpack Glance](https://developer.android.com/develop/ui/compose/glance) for Android.

## Setup

You can use any of the following options to work with the code here:

- [Fleet](https://www.jetbrains.com/fleet/): Jetbrains' new multiplatform-first IDE that's capable of working with all the languages in the project and running both the Android and iOS apps
- [Android Studio](https://developer.android.com/studio) Standard IDE for editing kotlin/Android code. With the [kotlin multiplatform plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform), you can run the code on both Android and iOS
- [Xcode](https://developer.apple.com/xcode/) allows for running the code on iOS, managing iOS simulators, and editing Swift code


## App store links

[Android](https://play.google.com/store/apps/details?id=com.desaiwang.transit.path)

[iOS](https://apps.apple.com/id/app/departures-widget-for-path/id6470330823?platform=iphone)

## Localizations

English and Spanish currently. If you want to contribute something else feel free. Translations are found in the following files:

- composeApp/src/commonMain/composeResources/values-{locale}/strings.xml
- iosApp/widget/Localizable.xcstrings (edit in Xcode)
- iosApp/iosApp/InfoPlist.xcstrings (edit in Xcode)
