plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "1.5.20"
}

kotlin {
  jvm()
  js {
    useCommonJs()
    browser()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
      }
    }
    val commonTest by getting
  }
}

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
}

