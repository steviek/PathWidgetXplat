plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
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
            implementation(libs.napier)
        }
    }
}

android {
    namespace = "com.sixbynine.transit.path.logging"

    buildFeatures {
        buildConfig = true
    }
}
