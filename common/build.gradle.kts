plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("17")) // Auto-download JDK for developers
  }
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "17"
    }
  }
  js {
    useCommonJs()
    browser()
    binaries.executable()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
      }
    }
    val commonTest by getting
  }
}
