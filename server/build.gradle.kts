import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

val ktor_version: String = "3.0.3"

dependencies {
  implementation(project(":common"))
  testImplementation(kotlin("test-junit"))

  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-server-sessions:$ktor_version")
  implementation("io.ktor:ktor-server-compression:$ktor_version")
  implementation("io.ktor:ktor-server-http-redirect:$ktor_version")
  implementation("io.ktor:ktor-server-forwarded-header:$ktor_version")
  implementation("io.ktor:ktor-server-status-pages:$ktor_version")
  implementation("io.ktor:ktor-server-cors:$ktor_version")
  implementation("io.ktor:ktor-server-call-logging:$ktor_version")
  implementation("io.ktor:ktor-server-default-headers:$ktor_version")
  implementation("io.ktor:ktor-server-resources:$ktor_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("io.ktor:ktor-server-servlet:$ktor_version")
  implementation("io.ktor:ktor-server-metrics:$ktor_version")
  implementation("io.ktor:ktor-server-html-builder:$ktor_version")
  implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")

  implementation("org.mongodb:mongodb-driver-sync:4.10.2")

  implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

  implementation("ch.qos.logback:logback-classic:1.5.16")

  implementation("commons-codec:commons-codec:1.16.0")
  implementation("com.github.studoverse:katerbase:3a04a4956ad131cc997ba9e623070da60527ca2f")
}

application {
  mainClass.set("com.studo.campusqr.ServerKt")
}

tasks {
  compileKotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
  }
  compileTestKotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
  }

  getByName<JavaExec>("run") {
    dependsOn(shadowJar)
    classpath(shadowJar)
  }

  shadowJar {
    setProperty("archiveFileName", "Server.jar")
  }

  // Copy Server.jar from build folder to root folder so we can delete all other folders
  register<Copy>("copyServer") {
    dependsOn(shadowJar)
    from(file("build/libs/Server.jar"))
    into(file("..")) // This build.grade file is in "server" subfolder, so "/server" is the current path
  }

  register("stage") {
    // Only copy production resources in stage command. During development, we don't need the Kotlin/JS production resources.
    rootProject.extra["runCopyProductionBuildToAllResources"] = true
    group = "distribution"
    dependsOn(getByName("copyServer"))
  }

  build {
    mustRunAfter(clean)
  }

  processResources {
    if (rootProject.extra.has("runCopyProductionBuildToAllResources")) {
      dependsOn(getByPath(":moderatorFrontend:copyProductionBuildToAllResources"))
    }
  }
}
