buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
  }
}

plugins {
  val kotlin_version = "2.0.10"
  kotlin("multiplatform") version kotlin_version apply false
  kotlin("jvm") version kotlin_version apply false
  kotlin("plugin.serialization") version kotlin_version apply false
}
