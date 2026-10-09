plugins {
    id("net.fabricmc.fabric-loom")
    `java-library`
}

version = "1.0.0+mc26.2"
group = "com.atsuishio.portlib"

base {
    archivesName.set("sbw-portlib")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    compileOnly("maven.modrinth:iris:1.11.4+26.2-fabric")
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
