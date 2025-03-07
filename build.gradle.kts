plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.0.20"
    id("io.github.goooler.shadow") version "8.1.8"
    id("com.willfp.libreforge-gradle-plugin") version "1.0.0"
}

group = "net.refractored"
version = findProperty("version")!!
val libreforgeVersion = findProperty("libreforge-version")

fun getGitHash(): String {
    var gitCommitHash = "unknown"
    try {
        val workingDir = File("${project.projectDir}")
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(workingDir)
            .start()
        process.waitFor()
        if (process.exitValue() == 0) {
            gitCommitHash = process.inputStream.bufferedReader().readText().trim()
        }
    } catch (e: Exception) {
    }
    return gitCommitHash
}

fun getCurrentGitBranch(): String {
    var gitBranch = "unknown"
    try {
        val workingDir = File("${project.projectDir}")
        val process = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
            .directory(workingDir)
            .start()
        process.waitFor()
        if (process.exitValue() == 0) {
            gitBranch = process.inputStream.bufferedReader().readText().trim()
        }
    } catch (e: Exception) {
    }
    return gitBranch
}

base {
    archivesName.set(project.name)
}

dependencies {
    project(":eco-core").dependencyProject.subprojects {
        implementation(this)
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "io.github.goooler.shadow")

    repositories {
        mavenLocal()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
    }

    dependencies {

        implementation("com.github.Revxrsal.Lamp:common:3.3.3")
        implementation("com.github.Revxrsal.Lamp:bukkit:3.3.3")

        compileOnly("net.kyori:adventure-platform-bukkit:4.1.2")
        implementation("net.kyori:adventure-text-minimessage:4.16.0")

        compileOnly("com.willfp:eco:6.75.2")
        compileOnly("org.jetbrains:annotations:23.0.0")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks {
        shadowJar {
            relocate("com.willfp.libreforge.loader", "net.refractored.bloodmoonreloaded.libreforge.loader")
        }

//        compileKotlin {
//            kotlinOptions {
//                jvmTarget = "17"
//            }
//        }


        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            val nonReleaseBranches = setOf("main", "master", "HEAD")
            if (getCurrentGitBranch() !in nonReleaseBranches) {
                version = "${version}-${getCurrentGitBranch()}-${getGitHash()}"
            }
            filesMatching(listOf("**plugin.yml", "**eco.yml")) {
                expand(
                    "version" to version,
                    "libreforgeVersion" to libreforgeVersion,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }
    }
}
