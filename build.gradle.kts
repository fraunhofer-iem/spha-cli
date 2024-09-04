plugins { kotlin("jvm") version "2.0.10" }

group = "de.fraunhofer.iem"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies { testImplementation(kotlin("test")) }

tasks.register("ktfmtCheck") { dependsOn(gradle.includedBuild("spha-cli").task(":ktfmtCheck")) }

tasks.register("ktfmtFormat") { dependsOn(gradle.includedBuild("spha-cli").task(":ktfmtFormat")) }

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }
