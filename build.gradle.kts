import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.crashlytics) apply false
}

subprojects {
    afterEvaluate {
        val kotlinExtension = extensions.findByName("kotlin")
        if (kotlinExtension is KotlinMultiplatformExtension) {
            with(kotlinExtension) {
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
            }
        }

        val androidExtension = extensions.findByName("android")
        (androidExtension as? BaseExtension)?.run {
            compileSdkVersion(libs.versions.android.compileSdk.get().toInt())

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}
