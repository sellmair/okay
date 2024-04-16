import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("kotlinx-atomicfu")
}

kotlin {
    jvmToolchain(17)
    jvm()
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":okay-fs"))
        implementation("io.ktor:ktor-client-cio:2.3.10")
        implementation("io.ktor:ktor-client-core:2.3.10")
        implementation("io.ktor:ktor-network:2.3.10")
        implementation("org.jetbrains.kotlinx:atomicfu:0.23.2")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.3")
    }

    sourceSets.jvmMain.dependencies {
        implementation("org.apache.maven:maven-model:3.9.6")
        implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.23")
        implementation("org.slf4j:slf4j-api:2.0.12")
        implementation("org.slf4j:slf4j-jdk14:2.0.12")
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test-junit5"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
        implementation("org.junit.jupiter:junit-jupiter")
    }
}


kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries {
            executable {
                entryPoint("io.sellmair.okay.main")
            }
        }
    }
}


tasks.register<Sync>("packageLibraries") {
    from(project.configurations.getByName(kotlin.jvm().compilations["main"].runtimeDependencyConfigurationName))
    into(layout.buildDirectory.dir("executable/libs"))
}

tasks.register<Jar>("packageJvmExecutable") {
    dependsOn("packageLibraries")

    from(kotlin.jvm().compilations["main"].output.allOutputs)
    destinationDirectory = layout.buildDirectory.dir("executable")
    archiveBaseName.set("okay")


    manifest {
        attributes["Main-Class"] = "io.sellmair.okay.ServerKt"
        attributes["Class-Path"] = project.provider {
            layout.buildDirectory.dir("executable/libs").get().asFile.listFiles().orEmpty()
                .joinToString(" ") { it.relativeTo(destinationDirectory.asFile.get()).path }
        }
    }
}

tasks.register<Copy>("packageNativeExecutable") {
    dependsOn(kotlin.macosArm64().binaries.getExecutable(NativeBuildType.DEBUG).linkTask)
    from(kotlin.macosArm64().binaries.getExecutable(NativeBuildType.DEBUG).linkTask.outputFile) {
        rename { "okay" }
    }
    into(layout.buildDirectory.file("executable"))
}

tasks.register("package") {
    dependsOn("packageLibraries")
    dependsOn("packageJvmExecutable")
    dependsOn("packageNativeExecutable")
}

/*
tasks.register<Sync>("install") {
    dependsOn("package")
    from(layout.buildDirectory.dir("executable"))
    into(Path(System.getProperty("user.home")).resolve(".okay").resolve("bin"))
}



tasks.register<JavaExec>("buildTestProject") {
    dependsOn("install")
    workingDir = rootDir.resolve("samples/ktorServer")
    classpath = kotlin.target.compilations["main"].runtimeDependencyFiles
    args = listOf("pkg")
    mainClass = "io.sellmair.okay.OkMain"
}


tasks.test.configure {
    useJUnitPlatform()
    workingDir(rootDir)
    outputs.upToDateWhen { false }
    testLogging {
        showStandardStreams = true
        events("started", "skipped", "failed")
        setExceptionFormat("full")
    }
}
*/

