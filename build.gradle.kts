plugins {
    kotlin("multiplatform") version "1.9.23" apply false
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.23.2")
    }
}