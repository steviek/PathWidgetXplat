plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }


    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.platform)
            implementation(projects.logging)
            implementation(projects.flipper)
            implementation(projects.schedule)

            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.ktor.core)
            implementation(libs.ktor.logging)

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
    namespace = "com.desaiwang.transit.path.api"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
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
