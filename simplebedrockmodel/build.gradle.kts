plugins {
    id("net.fabricmc.fabric-loom")
    `java-library`
}

version = "2.5.1+mc26.2"
group = "cn.sh1rocu"

base {
    archivesName.set("simplebedrockmodel-fabric")
}

loom {
    accessWidenerPath = file("src/main/resources/simplebedrockmodel.accesswidener")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("maven.modrinth:sodium:mc26.2-0.9.2-fabric")
    compileOnly("maven.modrinth:iris:1.11.4+26.2-fabric")
    api("com.maydaymemory:mae:1.1.4") {
        exclude("com.google.code.gson", "gson")
        exclude("com.google.code.findbugs", "jsr305")
        exclude("it.unimi.dsi", "fastutil")
        exclude("org.joml", "joml")
    }
    include("com.maydaymemory:mae:1.1.4")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "minecraft_version" to project.property("minecraft_version"),
        "loader_version" to project.property("loader_version")
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
