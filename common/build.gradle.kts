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
  js(LEGACY) {
    useCommonJs()
    browser()
    binaries.executable()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1") // 1.5.0 requires new IR compiler
      }
    }
    val commonTest by getting
  }
}
