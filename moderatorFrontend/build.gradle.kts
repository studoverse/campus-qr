import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
  kotlin("multiplatform")
}

val ktor_version: String = "2.3.2"
val kotlinx_html_version: String = "0.11.0"
val kotlinx_serialization_version: String = "1.6.2"
val kotlinx_coroutines_version: String = "1.7.3"

repositories {
  mavenCentral()
}

kotlin {
  js(IR) {
    useCommonJs()
    browser {
      testTask(Action {
        useKarma {
          useChrome() // Chrome must be installed (otherwise js tests can not be executed), it's also possible to use other browsers (e.g. useFirefox)
          webpackConfig.cssSupport {
            enabled.set(true)
          }
        }
      })
    }
    binaries.executable()
  }

  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(kotlin("stdlib-js"))
        implementation(project(":common"))

        api("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")

        api(libs.wrappers.react)
        api(libs.wrappers.react.dom)
        api(libs.wrappers.emotion)
        api(libs.wrappers.extensions)
        api(libs.wrappers.mui.material)
        api(libs.wrappers.mui.icons.material)
        api(libs.wrappers.mui.lab)

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
        api(npm("react", "18.3.1"))
        api(npm("react-dom", "18.3.1"))
        api(npm("@mui/material", "5.16.6"))
        api(npm("@mui/icons-material", "5.16.6"))

        api(npm("js-file-download", "0.4.12"))
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

  register("copyProductionBuildToAllResources") {
    dependsOn("copyProductionBuildToPostProcessedResources")
    dependsOn("copyProductionBuildToPreProcessedResources")
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