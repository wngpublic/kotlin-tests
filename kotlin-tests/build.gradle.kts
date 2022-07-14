import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "me"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("com.auth0:java-jwt:3.18.3")
    implementation("com.auth0:jwks-rsa:0.20.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("io.ktor:ktor-client-core-jvm:2.0.0")
    implementation("io.ktor:ktor-client-apache:2.0.0")
    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-client-cio:2.0.0")
    implementation("io.ktor:ktor-client-logging:2.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.0")
    // implementation("io.ktor:ktor-serialization-gson:2.0.0")
    implementation("org.json:json:20211205")
    implementation("io.arrow-kt:arrow-core:1.0.1")
    //runtimeOnly("io.arrow-kt:arrow-fx-coroutines:1.0.1")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.0.1")
    testImplementation(kotlin("test"))
    runtimeOnly("io.ktor:ktor-client-core:2.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}