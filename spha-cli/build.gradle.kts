plugins {
    jacoco
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.versions)
    application
}

group = "de.fraunhofer.iem.spha"

repositories { mavenCentral() }

dependencies {
    implementation(libs.bundles.kpiCalculator)

    implementation(libs.kotlin.cli)
    implementation(libs.kotlin.logging)

    implementation(libs.simpleLogger)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.di)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.di.test)
    testImplementation(libs.kotlin.di.junit5)
    testImplementation(libs.test.mocking)
    testImplementation(libs.test.fileSystem)
    testImplementation(libs.apache.commons)
    testImplementation(libs.test.junit5.params)
}

ktfmt {
    // KotlinLang style - 4 space indentation - From kotlinlang.org/docs/coding-conventions.html
    kotlinLangStyle()
}

tasks.test { useJUnitPlatform() }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports { xml.required = true }
}

tasks.register("jacocoReport") {
    description = "Generates code coverage reports for all test tasks."
    group = "Reporting"

    dependsOn(tasks.withType<JacocoReport>())
}

application { mainClass = "de.fraunhofer.iem.spha.cli.MainKt" }

kotlin {
    compilerOptions {
        jvmToolchain(21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}
