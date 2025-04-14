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
