plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform)
            implementation(libs.kotlin.coroutines)
            implementation(libs.kotlin.date.time)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.napier)
        }
    }
}

android {
    namespace = "com.desaiwang.transit.path.logging"

    buildFeatures {
        buildConfig = true
    }
}
