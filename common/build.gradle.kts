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
  js(IR) {
    useCommonJs()
    browser()
    binaries.executable()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
      }
    }
    val commonTest by getting
  }
}
