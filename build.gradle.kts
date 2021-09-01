buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
  }
}

plugins {
  kotlin("multiplatform") version "1.5.30" apply false
  kotlin("js") version "1.5.30" apply false
  kotlin("jvm") version "1.5.30" apply false
}

// Workaround. Hardcode the webpack version: 4rc0 works fine while 5 doesn't.
// Bug report & details https://youtrack.jetbrains.com/issue/KT-48273
// The fix was published in the experimental Kotlin version 1.5.30-382. TODO: Remove this workaround when 1.5.30 is stable
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
  rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().versions.webpackDevServer.version = "4.0.0-rc.0"
}