plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.api)
    implementation(projects.logging)
    implementation(projects.schedule)
    implementation(projects.platform)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.slf4j)
    implementation(libs.ksoup)

    testImplementation(projects.test)
    testImplementation(libs.kotlin.test)
}

application {
    mainClass.set("com.sixbynine.transit.path.schedule.generator.MainKt")
}
