rootProject.name = "okay"

include(":okay-core")
include(":okay-fs")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}