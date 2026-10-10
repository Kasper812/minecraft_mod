pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("net.fabricmc.fabric-loom") version "1.18.3"
        id("org.jetbrains.kotlin.jvm") version "2.4.21"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.4.21"
        id("com.google.devtools.ksp") version "2.3.12"
    }
}

rootProject.name = "sbw-26.2-fabric"

include(":portlib")
include(":geckolib4")
include(":simplebedrockmodel")
include(":ksp")
include(":superbwarfare")
include(":vvp")
include(":ashvehicle")
