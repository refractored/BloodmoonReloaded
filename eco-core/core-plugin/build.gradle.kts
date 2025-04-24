group = "net.refractored"
version = rootProject.version

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("io.papermc:paper-trail:1.0.1")
}

publishing {
    publications {
        register("maven", MavenPublication::class) {
            from(components["java"])
            artifactId = rootProject.name
        }
    }
}

tasks {
    build {
        dependsOn(publishToMavenLocal)
    }
}
