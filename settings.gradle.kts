rootProject.name = "CampusQR"

pluginManagement {
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "kotlinx-serialization") {
        useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
      }
    }
  }
}

dependencyResolutionManagement {
  repositories {
    maven("https://plugins.gradle.org/m2/")
    mavenCentral()
  }

  versionCatalogs {
    create("kotlinWrappers") {
      val wrappersVersion = "0.0.1-pre.825"
      from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
    }
  }
}

include("common")
include("moderatorFrontend")
include("server")