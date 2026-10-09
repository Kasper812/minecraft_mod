plugins {
    id("net.fabricmc.fabric-loom") apply false
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("com.google.devtools.ksp") apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.shedaniel.me") { name = "Shedaniel" }
        maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
        maven("https://maven.ladysnake.org/releases") { name = "Ladysnake" }
        maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") { name = "Fuzs" }
        maven("https://maven.blamejared.com/") {
            name = "BlameJared"
            content { includeGroup("mezz.jei") }
        }
        maven("https://api.modrinth.com/maven") {
            name = "Modrinth"
            content { includeGroup("maven.modrinth") }
        }
    }
}
