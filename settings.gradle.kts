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
      // TODO: @mh Doesn't render with v807 and above. Possible ktor issue?
      //  https://slack-chats.kotlinlang.org/t/18904299/hi-i-m-getting-irlinkageerror-can-not-read-value-from-variab
      val wrappersVersion = "0.0.1-pre.806"
      from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
    }
  }
}

include("common")
include("moderatorFrontend")
include("server")