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
    group = "testing"
    description = "Runs tests."
    dependsOn(gradle.includedBuild("spha-cli").task(":test"))
}
