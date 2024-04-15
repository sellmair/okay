import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    macosX64()
    macosArm64()
    linuxArm64()
    linuxX64()

    sourceSets.commonMain.dependencies {
        api("com.squareup.okio:okio:3.9.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation("com.squareup.okio:okio-fakefilesystem:3.9.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    }
}

tasks.withType<KotlinNativeTest>().configureEach {
    testLogging {
        this.showStandardStreams = true
        this.showCauses = true
        this.showExceptions = true
        this.showStackTraces = true
        this.exceptionFormat = TestExceptionFormat.FULL
    }


}