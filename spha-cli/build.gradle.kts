plugins {
    kotlin("jvm") version "2.0.10"
    application
}

group = "de.fraunhofer.iem"
version = "0.0.2-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies{
    implementation("de.fraunhofer.iem.kpiCalculator:core")
    implementation("de.fraunhofer.iem.kpiCalculator:model")
    implementation("de.fraunhofer.iem.kpiCalculator:adapter")
}

application{
    mainClass = "de.fraunhofer.iem.spha.cli.MainKt"
}