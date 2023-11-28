# Departure Widget for PATH

This is a [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) project targeting Android and iOS. Currently, only the iOS implementation is in this repository, but the the data and networking logic is in the shared code and could be used for Android as well.

## Setup

[Android Studio](https://developer.android.com/studio) Hedgehog or newer for editing the Kotlin code and running on Android. The JDK for the IDE should be Java 17.

[Xcode](https://developer.apple.com/xcode/) 15 or newer for editing the Swift code natively.

You _can_ run the iOS app from Android Studio, but usually you'd want to run the widget extension target in Xcode, and I don't think you can do that from AS.

## App store links

[Android](https://play.google.com/store/apps/details?id=com.sixbynine.transit.path)

iOS: Coming soon

## Localizations

English and Spanish currently. If you want to contribute something else feel free. Translations are found in the following files:

- composeApp/src/commonMain/resources/MR/{locale}/strings.xml
- iosApp/widget/Localizable.xcstrings (edit in Xcode)
- iosApp/iosApp/InfoPlist.xcstrings (edit in Xcode)
