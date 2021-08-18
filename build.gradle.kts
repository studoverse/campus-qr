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

// Workaround. Hardcode the webpack version: 4rc0 works fine while 5 doesn't. Remove when fixed.
// Bug report & details https://youtrack.jetbrains.com/issue/KT-48273
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
  rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.webpackDevServer.version = "4.0.0-rc.0"
}