buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
  }
}

plugins {
  kotlin("multiplatform") version "1.4.32" apply false
  kotlin("js") version "1.4.31" apply false
  kotlin("jvm") version "1.4.31" apply false
}

group = "com.studo"
version = "1.0.0"

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
  maven("https://jitpack.io")
}