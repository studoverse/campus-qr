pluginManagement {
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "kotlinx-serialization") {
        useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
      }
    }
  }

  repositories {
    maven("https://plugins.gradle.org/m2/")
    mavenCentral()
  }
}

rootProject.name = "CampusQR"

include("common")
include("moderatorFrontend")
include("server")