plugins {
  kotlin("multiplatform")
}

kotlin {
  jvm {
    withJava()
  }
  js {
    browser()
  }

  sourceSets {
    val commonMain by getting
    val commonTest by getting
  }
}

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
  maven("https://jitpack.io")
}

