plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.12")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
