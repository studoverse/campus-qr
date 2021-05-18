plugins {
  kotlin("multiplatform")
}

kotlin {
  jvm()
  js {
    useCommonJs()
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
}

