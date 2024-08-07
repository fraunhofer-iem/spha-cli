import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.plugin.allopen") version "2.0.20-Beta2"
    kotlin("jvm") version "2.0.0"
}

group = "de.fraunhofer.iem"
version = "0.0.2-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}
