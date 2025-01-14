@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of("21")) // Auto-download JDK for developers
  }
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "21"
    }
  }
  js(IR) {
    browser()
    compilerOptions {
      target = "es2015"
    }
    binaries.executable()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
      }
    }
    val commonTest by getting
  }
}
