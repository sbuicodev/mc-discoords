pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Applies fabric-loom-remap on obfuscated versions (1.21.x) and fabric-loom on 26.1+
    id("dev.kikugie.loom-back-compat") version "0.4.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("1.21.11")
        // The 26.1 node targets the latest 26.1.x patch
        version("26.1", "26.1.2")
        versions("26.2", "26.3")
        vcsVersion = "26.3"
    }
}

rootProject.name = "discoords"
