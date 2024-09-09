plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" }

rootProject.name = "software-product-health-analyzer-cli"

// Order matters!
includeBuild("library")

includeBuild("spha-cli")
