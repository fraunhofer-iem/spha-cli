repositories { mavenCentral() }

tasks.register("ktfmtCheck") {
    group = "formatting"
    description = "Checks code formatting."
    dependsOn(gradle.includedBuild("spha-cli").task(":ktfmtCheck"))
}

tasks.register("ktfmtFormat") {
    group = "formatting"
    description = "Applies ktfmt's formatting rules."
    dependsOn(gradle.includedBuild("spha-cli").task(":ktfmtFormat"))
}

tasks.register("test") {
    group = "verification"
    description = "Runs tests."
    dependsOn(gradle.includedBuild("spha-cli").task(":test"))
}

tasks.register("dependencyUpdates") {
    group = "dependencies"
    description = "Prints possible dependency updates."
    dependsOn(gradle.includedBuild("spha-cli").task(":dependencyUpdates"))
}

tasks.register("clean") {
    group = "build"
    description = "Removes all builds."
    dependsOn(gradle.includedBuild("spha-cli").task(":clean"))
}

tasks.register("build") {
    group = "build"
    description = "Build the service"
    dependsOn(gradle.includedBuild("spha-cli").task(":build"))
}

tasks.register("assemble") {
    group = "build"
    description = "Assembles everything into a deployable format."
    dependsOn(gradle.includedBuild("spha-cli").task(":assemble"))
}
