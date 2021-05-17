buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
  }
}

plugins {
  kotlin("multiplatform") version "1.4.31" apply false
  kotlin("js") version "1.4.31" apply false
  kotlin("jvm") version "1.4.31" apply false
}