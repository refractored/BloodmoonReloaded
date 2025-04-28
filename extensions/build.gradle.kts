import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks.named("jar") {
    enabled = true
}

subprojects {
    val extensionName = findProperty("extension.name") ?: "invalid"
    val version = findProperty("extension.version") ?: "0.1-SNAPSHOT"

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
        compileOnly(project(":eco-core")){
            isTransitive = false
        }
        compileOnly(project(":eco-core:core-plugin")){
            isTransitive = false
        }
    }

    tasks {

        build {
            dependsOn(shadowJar)
        }

        withType<ShadowJar> {
            destinationDirectory.set(file("$rootDir/bin"))

            archiveFileName.set("$extensionName-v$version.jar")
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
