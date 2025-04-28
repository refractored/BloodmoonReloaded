import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

fun compareVersions(version1: String, version2: String): Int {
    val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
    val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }
    val maxLength = maxOf(parts1.size, parts2.size)

    for (i in 0 until maxLength) {
        val part1 = parts1.getOrElse(i) { 0 }
        val part2 = parts2.getOrElse(i) { 0 }
        if (part1 != part2) {
            return part1 - part2
        }
    }
    return 0
}

tasks.named("jar") {
    enabled = true
}

val baseMinimumVersion = findProperty("extension.minimumVersion") ?: "1.0"

subprojects {
    val extensionName = findProperty("extension.name") ?: "invalid"
    val version = findProperty("extension.version") ?: "0.1-SNAPSHOT"
    var minimumVersion = findProperty("extension.minimumVersion") ?: baseMinimumVersion

    if (compareVersions(baseMinimumVersion.toString(), minimumVersion.toString()) > 0) {
        minimumVersion = baseMinimumVersion
    }

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
        withType<ShadowJar> {
            destinationDirectory.set(file("$rootDir/bin/extensions"))

            archiveFileName.set("$extensionName-v$version.jar")
        }

        processResources {
            filesMatching("extension.yml") {
                expand(
                    mapOf(
                        "name" to extensionName,
                        "version" to version,
                        "minVersion" to minimumVersion
                    )
                )
            }
        }
    }
}
