plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    application
}

group = "de.fraunhofer.iem.spha"
version = "0.0.2-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies{
    implementation("de.fraunhofer.iem.kpiCalculator:core")
    implementation("de.fraunhofer.iem.kpiCalculator:model")
    implementation("de.fraunhofer.iem.kpiCalculator:adapter")

    implementation("com.github.ajalt.clikt:clikt:4.4.0")

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.14")
    implementation(libs.kotlin.serialization.json)

    implementation("io.insert-koin:koin-core:3.5.6")

    testImplementation(libs.kotlin.test)
    testImplementation("io.insert-koin:koin-test:3.5.6")
    testImplementation("io.insert-koin:koin-test-junit5:3.5.6")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("com.google.jimfs:jimfs:1.3.0")
    testImplementation("org.apache.commons:commons-lang3:3.16.0")


}

tasks.test {
    useJUnitPlatform()
}

application{
    mainClass = "de.fraunhofer.iem.spha.cli.MainKt"
}

kotlin {
    compilerOptions {
        jvmToolchain(21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

