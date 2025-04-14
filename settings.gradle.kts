pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://repo.jpenilla.xyz/snapshots/")
        maven("https://repo.auxilor.io/repository/maven-public/")
    }
}

rootProject.name = "BloodmoonReloaded"

// Core
include(":eco-core")
include(":eco-core:core-plugin")

include("extensions")
File(rootDir, "extensions").listFiles()
    ?.filter { it.isDirectory && it.name != "build" }
    ?.forEach { include("extensions:${it.name}") }
