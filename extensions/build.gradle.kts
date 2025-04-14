import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks.named("jar") {
    enabled = true
}

subprojects {
    val extensionName = findProperty("expansion.name") ?: "invalid"
    val version = findProperty("expansion.version") ?: "0.1-SNAPSHOT"

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
        compileOnly(project(":eco-core"))
        compileOnly(project(":eco-core:core-plugin"))

        if (project.name == "drops") {
            compileOnly(project(":extensions:hordes"))
        }
    }

    tasks {
        named<Jar>("jar") {
            archiveFileName.set("$extensionName.jar")
        }

        withType<ShadowJar> {
            // Exclude libraries from being bundled into the JAR
            dependencies {
                exclude(dependency("io.github.revxrsal:lamp.common"))
                exclude(dependency("io.github.revxrsal:lamp.bukkit"))
                exclude(dependency("io.github.revxrsal:lamp.brigadier"))
                exclude(dependency("org.json:json"))
                exclude(dependency("net.kyori:adventure-platform-bukkit"))
                exclude(dependency("net.kyori:adventure-text-minimessage"))
            }
        }

        processResources {
            filesMatching("extension.yml") {
                expand(
                    mapOf(
                        "name" to extensionName,
                        "version" to version
                    )
                )
            }
        }
    }
}
