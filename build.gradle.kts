buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
  }
}

plugins {
  val kotlin_version = "1.9.0"
  kotlin("multiplatform") version kotlin_version apply false
  kotlin("jvm") version kotlin_version apply false
  kotlin("js") version kotlin_version apply false
  kotlin("plugin.serialization") version kotlin_version apply false
}
