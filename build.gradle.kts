import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
  kotlin("multiplatform") version "1.4.32"
  application
  id("com.github.johnrengelman.shadow") version "5.0.0"
}
group = "com.studo"
version = "1.0.0"

repositories {
  mavenCentral()
  jcenter()
  maven {
    url = uri("https://dl.bintray.com/kotlin/ktor")
  }
  maven {
    url = uri("https://dl.bintray.com/kotlin/kotlinx")
  }
  maven {
    url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers")
  }
  maven("https://jitpack.io")
}
kotlin {
  jvm("server") {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
    withJava()
  }
  js("moderatorFrontend") {
    useCommonJs()
    browser {
      binaries.executable()
    }
  }
  sourceSets {
    val commonMain by getting
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }
    val serverMain by getting {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("io.ktor:ktor-server-netty:1.4.0")
        implementation("io.ktor:ktor-html-builder:1.4.0")
        implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

        implementation("org.mongodb:mongodb-driver-sync:4.1.0")

        implementation("com.fasterxml.jackson.core:jackson-core:2.11.2")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")

        implementation("ch.qos.logback:logback-classic:1.2.3")

        implementation("commons-codec:commons-codec:1.14")
        implementation("com.github.studo-app:katerbase:abc9891909")
      }
    }
    val serverTest by getting {
      dependencies {
        implementation(kotlin("test-junit"))
      }
    }
    val moderatorFrontendMain by getting {
      dependencies {
        api(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
        api("org.jetbrains:kotlin-react:16.13.1-pre.115-kotlin-1.4.10")
        api("org.jetbrains:kotlin-react-dom:16.13.1-pre.115-kotlin-1.4.10")
        api("org.jetbrains:kotlin-extensions:1.0.1-pre.115-kotlin-1.4.10")
        implementation(npm("normalize.css", "^8.0.1"))

        api(npm("react", "^16.5.2"))
        api(npm("react-dom", "^16.5.2"))

        api(npm("@material-ui/core", "^4.10.2"))
        api(
          npm(
            "@material-ui/lab",
            "^4.0.0-alpha.56"
          )
        ) // Needed for MuiAutocomplete. Can be removed if it gets merged into @material-ui/core
        api(npm("@material-ui/icons", "^3.0.1"))
        api(npm("@material-ui/pickers", "3.2.10"))
        // don't update to latest version until @material-ui/pickers v4 comes out
        api(npm("@date-io/luxon", "1.3.13"))
        api(npm("luxon", "1.24.1"))

        api(npm("js-file-download", "^0.4.12"))
      }
    }
    val moderatorFrontendTest by getting {
      dependencies {
        implementation(kotlin("test-js"))
      }
    }
  }
}
application {
  mainClassName = "com.studo.campusqr.ServerKt"
}
tasks.getByName<KotlinWebpack>("moderatorFrontendBrowserProductionWebpack") {
  outputFileName = "campusqr-admin.js"
}
tasks.getByName<Jar>("serverJar") {
  dependsOn(tasks.getByName("moderatorFrontendBrowserProductionWebpack"))
  val moderatorFrontendBrowserProductionWebpack =
    tasks.getByName<KotlinWebpack>("moderatorFrontendBrowserProductionWebpack")
  from(
      File(
          moderatorFrontendBrowserProductionWebpack.destinationDirectory,
          moderatorFrontendBrowserProductionWebpack.outputFileName
      )
  )
}
tasks.getByName<JavaExec>("run") {
  dependsOn(tasks.getByName<Jar>("serverJar"))
  classpath(tasks.getByName<Jar>("serverJar"))
}

tasks.getByName("shadowJar") {
  dependsOn(tasks.getByName("serverJar"))
  setProperty("archiveFileName", "Server.jar")
}

tasks.register("stage") {
  group = "distribution"
  dependsOn(tasks.getByName("shadowJar"))
}