plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform)
            implementation(projects.logging)
            implementation(projects.flipper)
            implementation(projects.schedule)

            implementation(libs.ktor.core)
            implementation(libs.ktor.logging)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)

            api(libs.kotlin.date.time)
            implementation(libs.kotlin.coroutines)
            implementation(libs.kotlin.serialization.json)
        }

        commonTest.dependencies {
            implementation(projects.test)
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.okhttp)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.okhttp)
            implementation(libs.slf4j)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        iosMain.dependencies {
            implementation(libs.ktor.darwin)
        }

        all {
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

android {
    namespace = "com.sixbynine.transit.path.api"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        coreLibraryDesugaring(libs.android.tools.desugar)
        testImplementation(libs.junit)
    }
}
