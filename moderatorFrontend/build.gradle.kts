plugins {
  kotlin("js")

  // Workaround for https://youtrack.jetbrains.com/issue/KT-51921 respectively https://github.com/JetBrains/kotlin-wrappers/issues/1077
  id("io.github.turansky.kfc.legacy-union") version "5.8.0"
}

val ktor_version: String = "2.0.2"
val kotlinx_html_version: String = "0.7.3"
val kotlinx_serialization_version: String = "1.3.1"
val kotlinx_coroutines_version: String = "1.5.2"

repositories {
  mavenCentral()
}

fun kotlinw(target: String): String = "org.jetbrains.kotlin-wrappers:kotlin-$target"
val kotlinWrappersVersion = "0.0.1-pre.331-kotlin-1.6.20"

kotlin {
  js {
    useCommonJs()
    browser()
    binaries.executable()
  }
}

// Fixes webpack-cli incompatibility by pinning the newest version.
rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
  versions.webpackCli.version = "4.10.0"
}

dependencies {
  implementation(kotlin("stdlib-js"))
  implementation(project(":common"))
  implementation(enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))

  api("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")
  api(kotlinw("react"))
  api(kotlinw("emotion"))
  api(kotlinw("react-dom"))
  api(kotlinw("mui"))
  api(kotlinw("mui-icons"))
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
  api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
  api("io.ktor:ktor-client-content-negotiation:$ktor_version")

  implementation(npm("@emotion/react", "11.7.1"))
  implementation(npm("@emotion/styled", "11.6.0"))

  // kotlinx-serialization + Ktor client
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
  implementation("io.ktor:ktor-client-core:$ktor_version")
  implementation("io.ktor:ktor-client-serialization:$ktor_version")

  implementation(npm("normalize.css", "8.0.1"))
  implementation(devNpm("style-loader", "2.0.0"))
  implementation(devNpm("css-loader", "5.2.6"))

  api(npm("react", "18.0.0"))
  api(npm("react-dom", "18.0.0"))

  api(npm("@mui/material", "5.6.1"))

  api(npm("js-file-download", "0.4.12"))

  testImplementation(kotlin("test-js"))
}

tasks {
  getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("browserProductionWebpack") {
    outputFileName = "campusqr-admin.js"
  }

  register<Copy>("copyProductionBuildToPreProcessedResources") {
    dependsOn("browserProductionWebpack") // Build production version
    from("build/distributions")
    into("../server/src/main/resources/moderatorFrontend/")
  }

  register<Copy>("copyProductionBuildToPostProcessedResources") {
    dependsOn("browserProductionWebpack") // Build production version
    from("build/distributions")
    into("../server/build/resources/main/moderatorFrontend/")
  }

  register("copyProductionBuildToAllResources") {
    dependsOn("copyProductionBuildToPostProcessedResources")
    dependsOn("copyProductionBuildToPreProcessedResources")
  }
}