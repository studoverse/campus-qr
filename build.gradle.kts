buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
  }
}

plugins {
  kotlin("multiplatform") version "1.5.10" apply false
  kotlin("js") version "1.5.10" apply false
  kotlin("jvm") version "1.5.10" apply false
}