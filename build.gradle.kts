import kotlin.io.path.Path

plugins {
    //kotlin("jvm") version "2.0.0-Beta5"
    kotlin("jvm") version "1.9.21"
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

tasks.register<Sync>("packageLibraries") {
    from(project.configurations.getByName(kotlin.target.compilations["main"].runtimeDependencyConfigurationName))
    into(layout.buildDirectory.dir("executable/libs"))
}

tasks.register<Jar>("packageExecutable") {
    dependsOn("packageLibraries")

    from(kotlin.target.compilations["main"].output.allOutputs)
    destinationDirectory = layout.buildDirectory.dir("executable")

    manifest {
        attributes["Main-Class"] = "io.sellmair.okay.OkMain"
        attributes["Class-Path"] = project.provider {
            layout.buildDirectory.dir("executable/libs").get().asFile.listFiles().orEmpty()
                .joinToString(" ") { it.relativeTo(destinationDirectory.asFile.get()).path }
        }
    }
}

tasks.register("package") {
    dependsOn("packageLibraries")
    dependsOn("packageExecutable")
}

tasks.register<Sync>("install") {
    dependsOn("package")
    from(layout.buildDirectory.dir("executable"))
    into(Path(System.getProperty("user.home")).resolve(".okay").resolve("bin"))
}

tasks.register<JavaExec>("buildTestProject") {
    dependsOn("install")
    workingDir = file("testProject")
    classpath = kotlin.target.compilations["main"].runtimeDependencyFiles
    args = listOf("build")
    mainClass = "io.sellmair.okay.OkMain"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.23")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}