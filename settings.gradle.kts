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
include("extensions:hordes")
include("extensions:drops")
