plugins {
  kotlin("jvm")
  java
  application
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  implementation(project(":common"))
  implementation("io.ktor:ktor-server-netty:1.6.3")
  implementation("io.ktor:ktor-html-builder:1.6.4")
  implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

  implementation("org.mongodb:mongodb-driver-sync:4.1.0")

  implementation("com.fasterxml.jackson.core:jackson-core:2.11.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")

  implementation("ch.qos.logback:logback-classic:1.2.3")

  implementation("commons-codec:commons-codec:1.14")
  implementation("com.github.studo-app:katerbase:418f2e6ce2")

  testImplementation(kotlin("test-junit"))
}

application {
  mainClassName = "com.studo.campusqr.ServerKt"
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "11"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "11"
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
    group = "distribution"
    dependsOn(getByName("copyServer"))
  }

  build {
    mustRunAfter(clean)
  }

  processResources {
    dependsOn(getByPath(":moderatorFrontend:copyProductionBuildToAllResources"))
  }
}
