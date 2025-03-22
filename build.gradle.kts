import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.5"
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

fun isGitDirty(): Boolean = try {
    val workingDir = File("${project.projectDir}")
    val process = ProcessBuilder("git", "diff", "--quiet", "--ignore-submodules=dirty")
        .directory(workingDir)
        .start()
    process.waitFor()
    process.exitValue() != 0
} catch (e: Exception) {
    false
}

fun getCurrentGitTag(): String? {
    var gitTag: String? = null
    try {
        val workingDir = File("${project.projectDir}")
        val process = ProcessBuilder("git", "describe", "--tags", "--exact-match")
            .directory(workingDir)
            .start()
        process.waitFor()
        if (process.exitValue() == 0) {
            gitTag = process.inputStream.bufferedReader().readText().trim()
        }
    } catch (e: Exception) {
    }
    return gitTag
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
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenLocal()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://jitpack.io")
    }

    dependencies {

        implementation("io.github.revxrsal:lamp.common:4.0.0-rc.9")
        implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.9")
        implementation("io.github.revxrsal:lamp.brigadier:4.0.0-rc.9")

        compileOnly("net.kyori:adventure-platform-bukkit:4.1.2")
        compileOnly("net.kyori:adventure-text-minimessage:4.16.0")

        implementation("org.json:json:20250107")

        compileOnly("com.willfp:eco:6.75.2")
        compileOnly("org.jetbrains:annotations:23.0.0")
        compileOnly(kotlin("stdlib", version = "2.1.0"))
    }

    java {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile> {
        // Preserve parameter names in the bytecode
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            javaParameters = true
        }
    }

    tasks {


        if (getCurrentGitTag()?.removePrefix("v") != version) {
            version = "${version}-${getGitHash()}"
        }
        if (isGitDirty()) {
            version = "${version}-dirty"
        }

        shadowJar {
            relocate("org.json", "net.refractored.libs.json")
            relocate("revxrsal.commands", "net.refractored.libs.lamp")
            relocate("com.willfp.libreforge.loader", "net.refractored.bloodmoonreloaded.libreforge.loader")
        }

        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
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
