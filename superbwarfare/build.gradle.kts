plugins {
    id("net.fabricmc.fabric-loom")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    `java-library`
}

val modVersion = "0.8.9.1"
version = "$modVersion+mc26.2"
group = "com.atsuishio.superbwarfare"

base {
    archivesName.set("superbwarfare")
}

repositories {
    flatDir { dirs("libs") }
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
}

loom {
    accessWidenerPath = file("src/main/resources/superbwarfare.accesswidener")
    mods {
        create("superbwarfare") {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

dependencies {
    add("ksp", project(":ksp"))
    compileOnly(project(":ksp"))

    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    implementation(project(":portlib"))
    include(project(":portlib"))
    implementation(project(":simplebedrockmodel"))
    include(project(":simplebedrockmodel"))
    implementation(project(":geckolib4"))
    include(project(":geckolib4"))
    implementation("maven.modrinth:trinkets-updated:4.1.1+26.2")
    implementation("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
        exclude("net.fabricmc.fabric-api")
    }
    api("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${property("forge_config_api_port_version")}")

    include(implementation("org.ladysnake.cardinal-components-api:cardinal-components-base:${property("cardinal_components_version")}")!!)
    include(implementation("org.ladysnake.cardinal-components-api:cardinal-components-entity:${property("cardinal_components_version")}")!!)
    include(api("teamreborn:energy:5.0.0")!!)

    implementation(files("libs/rhino-1.8.1-SNAPSHOT.jar"))
    include(files("libs/rhino-1.8.1-SNAPSHOT.jar"))

    implementation("com.maydaymemory:mae:1.1.4") {
        exclude("com.google.code.gson", "gson")
        exclude("com.google.code.findbugs", "jsr305")
        exclude("it.unimi.dsi", "fastutil")
        exclude("org.joml", "joml")
    }
    include("com.maydaymemory:mae:1.1.4")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("mezz.jei:jei-26.2-fabric-api:${property("jei_version")}")
    compileOnly("maven.modrinth:jade:26.2.11+fabric")
    compileOnly("maven.modrinth:iris:1.11.4+26.2-fabric")
    compileOnly("maven.modrinth:sodium:mc26.2-0.9.2-fabric")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.WARN
    val props = mapOf(
        "version" to project.version,
        "minecraft_version" to project.property("minecraft_version"),
        "loader_version" to project.property("loader_version"),
        "mod_id" to "superbwarfare",
        "mod_name" to "Superb Warfare",
        "mod_license" to "GNU General Public License v3.0",
        "mod_version" to modVersion,
        "mod_authors" to "Atsuishio, Roki27, Light_Quanta, CrawKatt and other contributors",
        "mod_description" to "A Warfare Mod"
    )
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
    withSourcesJar()
}

kotlin {
    jvmToolchain(25)
    sourceSets {
        named("main") {
            kotlin.srcDirs("src/main/kotlin", "src/main/java")
        }
    }
}
