plugins {
    // Applies the correct loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

// Libraries that aren't provided by the game, bundled inside the mod jar along with their dependencies
val bundled: Configuration by configurations.creating
configurations.implementation { extendsFrom(bundled) }

// Already provided by Minecraft, so not nested
val providedByGame = setOf("org.slf4j", "com.google.code.gson", "com.google.errorprone", "org.jetbrains:annotations")

// Loom's `include` isn't transitive, so every resolved module of `bundled` is added to it explicitly
configurations.include {
    dependencies.addAllLater(bundled.incoming.artifacts.resolvedArtifacts.map { artifacts ->
        artifacts.map { it.id.componentIdentifier }
            .filterIsInstance<ModuleComponentIdentifier>()
            .filterNot { it.group in providedByGame || "${it.group}:${it.module}" in providedByGame }
            .map { project.dependencies.create("${it.group}:${it.module}:${it.version}") }
    })
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang Mappings on obfuscated versions
    loomx.applyMojangMappings()

    // Use `mod{dependency type}` even on 26.1+ - loom-back-compat converts them
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    // The whole Fabric API, since fabric.mod.json depends on the `fabric-api` mod
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    bundled("net.dv8tion:JDA:${property("deps.jda")}") {
        exclude(module = "opus-java") // Voice support, unused
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", project.version.toString())
            register("minecraft", sc.properties["mod.mc_compat"])
            register("java", requiredJava.majorVersion)
            register("loader", Regex("\\d+\\.\\d+").find(sc.properties.get<String>("deps.fabric_loader"))!!.value)
        }

        filesMatching("fabric.mod.json") { expand(props) }
    }

    // Includes the license file in the built mod
    withType<Jar> {
        val name = project.property("mod.id")
        inputs.property("mod_id", name)
        from("../../LICENSE") { rename { "${it}_$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // loomx.mod(Sources)Jar returns the jar task for the applied loom variant
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
