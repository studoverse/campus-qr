buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
  }
}

plugins {
  val kotlinVersion = "1.8.10"
  kotlin("multiplatform") version kotlinVersion apply false
  kotlin("jvm") version kotlinVersion apply false
  kotlin("js") version kotlinVersion apply false
  kotlin("plugin.serialization") version kotlinVersion apply false
}
