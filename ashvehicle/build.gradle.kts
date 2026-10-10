plugins {
    id("net.fabricmc.fabric-loom")
    `java-library`
}

val modVersion = "4.5.3"
version = "$modVersion+mc26.2"
group = "Aru.Aru.ashvehicle"

base {
    archivesName.set("ashvehicle")
}

repositories {
    maven(uri("../superbwarfare/libs/maven")) { name = "LocalLibs" }
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
}

loom {
    mods {
        create("ashvehicle") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    implementation(project(":superbwarfare"))
    implementation(project(":portlib"))
    implementation(project(":simplebedrockmodel"))
    implementation(project(":geckolib4"))
    implementation("maven.modrinth:trinkets-updated:4.1.1+26.2")
    implementation("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
        exclude("net.fabricmc.fabric-api")
    }
    implementation("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${property("forge_config_api_port_version")}")
    implementation("org.ladysnake.cardinal-components-api:cardinal-components-base:${property("cardinal_components_version")}") { exclude(group = "net.fabricmc.fabric-api") }
    implementation("org.ladysnake.cardinal-components-api:cardinal-components-entity:${property("cardinal_components_version")}") { exclude(group = "net.fabricmc.fabric-api") }
    implementation("teamreborn:energy:5.0.0")
    implementation("local.sbw:rhino:1.8.1")
    implementation("com.maydaymemory:mae:1.1.4") {
        exclude("com.google.code.gson", "gson")
        exclude("com.google.code.findbugs", "jsr305")
        exclude("it.unimi.dsi", "fastutil")
        exclude("org.joml", "joml")
    }
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("mezz.jei:jei-26.2-fabric-api:${property("jei_version")}")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.WARN
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") { expand(props) }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "100000"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
