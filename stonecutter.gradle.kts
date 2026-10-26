plugins {
    id("dev.kikugie.stonecutter")
    // Loom variants picked by loom-back-compat for each version
    id("net.fabricmc.fabric-loom-remap") version "1.19.3" apply false
    id("net.fabricmc.fabric-loom") version "1.19.3" apply false
}

stonecutter active "26.3"
