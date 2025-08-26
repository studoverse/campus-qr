@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
  kotlin("multiplatform")
  id("io.github.turansky.seskar") version "4.14.0"
}

val ktor_version: String = "3.0.3" // https://github.com/ktorio/ktor/releases
val kotlinx_html_version: String = "0.11.0" // https://github.com/Kotlin/kotlinx.html/releases
val kotlinx_serialization_version: String = "1.8.0" // https://github.com/Kotlin/kotlinx.serialization/releases
val kotlinx_coroutines_version: String = "1.9.0" // https://github.com/Kotlin/kotlinx.coroutines/releases

repositories {
  mavenCentral()
}

kotlin {
  js(IR) {
    browser {
      commonWebpackConfig(body = Action {
        cssSupport {
          enabled.set(true)
        }
      })

      testTask(Action {
        useKarma {
          useChrome() // Chrome must be installed (otherwise js tests can not be executed), it's also possible to use other browsers (e.g. useFirefox)
          webpackConfig.cssSupport {
            enabled.set(true)
          }
        }
      })
    }
    compilerOptions {
      target = "es2015"
    }
    binaries.executable()
  }

  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(kotlin("stdlib-js"))
        implementation(project(":common"))

        api("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")

        api(kotlinWrappers.react)
        api(kotlinWrappers.reactDom)
        api(kotlinWrappers.emotion.react)
        api(kotlinWrappers.emotion.styled)
        api(kotlinWrappers.mui.material)
        api(kotlinWrappers.mui.iconsMaterial)
        api(kotlinWrappers.mui.lab)

        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
        api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
        api("io.ktor:ktor-client-content-negotiation:$ktor_version")

        // kotlinx-serialization + Ktor client
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
        implementation("io.ktor:ktor-client-core:$ktor_version")
        implementation("io.ktor:ktor-client-serialization:$ktor_version")

        implementation(npm("normalize.css", "8.0.1"))
        api(devNpm("style-loader", "3.3.3"))
        api(devNpm("css-loader", "6.8.1"))

        // Use versions that are specified in the kotlin-wrappers: https://github.com/JetBrains/kotlin-wrappers/blob/master/gradle.properties
        api(npm("react", "19.1.1"))
        api(npm("react-dom", "19.1.1"))
        api(npm("@mui/material", "5.16.6"))
        api(npm("@mui/icons-material", "5.16.6"))

        api(npm("js-file-download", "0.4.12"))

        api(devNpm("webpack-bundle-analyzer", "4.10.2"))
      }
    }
    val jsTest by getting {
      dependencies {
        implementation(kotlin("test-js"))
      }
    }
  }
}

tasks {
  getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack") {
    mainOutputFileName.set("campusqr-admin.js")
  }

  register<Copy>("copyProductionBuildToPreProcessedResources") {
    dependsOn("jsBrowserProductionWebpack") // Build production version
    from("build/kotlin-webpack/js/productionExecutable")
    into("../server/src/main/resources/moderatorFrontend/")
  }

  register<Copy>("copyProductionBuildToPostProcessedResources") {
    dependsOn("jsBrowserProductionWebpack") // Build production version
    from("build/kotlin-webpack/js/productionExecutable")
    into("../server/build/resources/main/moderatorFrontend/")
  }

  register<Copy>("copyResourcesToPreProcessedResources") {
    dependsOn("jsBrowserProductionWebpack") // Build production version
    from("build/processedResources/js/main/importCss.js")
    into("../server/src/main/resources/moderatorFrontend/")
  }

  register<Copy>("copyResourcesToPostProcessedResources") {
    dependsOn("jsBrowserProductionWebpack") // Build production version
    from("build/processedResources/js/main/importCss.js")
    into("../server/build/resources/main/moderatorFrontend/")
  }

  register("copyProductionBuildToAllResources") {
    dependsOn("copyProductionBuildToPreProcessedResources")
    dependsOn("copyProductionBuildToPostProcessedResources")
    dependsOn("copyResourcesToPreProcessedResources")
    dependsOn("copyResourcesToPostProcessedResources")
  }
}

afterEvaluate {
  with(yarn) {
    // Override versions of transitive dependencies that are used by the kotlin gradle plugin.
    // Reference: https://kotlinlang.slack.com/archives/C3PQML5NU/p1650464363351559?thread_ts=1639839062.315600&cid=C3PQML5NU
    resolution("minimist", "^1.2.6")
    resolution("ua-parser-js", "^0.7.33")
    resolution("socket.io", "^4.6.0")
    resolution("minimatch", "^3.0.5")
  }
}