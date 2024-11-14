/*
 * Copyright (c) 2024 Fraunhofer IEM. All rights reserved.
 *
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 *
 * SPDX-License-Identifier: MIT
 * License-Filename: LICENSE
 */

plugins {
    jacoco
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.versions)
    alias(libs.plugins.semver)
    application
}

group = "de.fraunhofer.iem"

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

semver {
    // Do not create an empty release commit when running the "releaseVersion" task.
    createReleaseCommit = false

    // Do not let untracked files bump the version or add a "-SNAPSHOT" suffix.
    noDirtyCheck = true
}

// Only override a default version (which usually is "unspecified"), but not a custom version.
if (version == Project.DEFAULT_VERSION) {
    version =
        semver.semVersion
            .takeIf { it.isPreRelease }
            // To get rid of a build part's "+" prefix because Docker tags do not support it, use
            // only the original "build"
            // part as the "pre-release" part.
            ?.toString()
            ?.replace("${semver.defaultPreRelease}+", "")
            // Fall back to a plain version without pre-release or build parts.
            ?: semver.version
}

logger.lifecycle("Building SPHA-CLI version $version.")
