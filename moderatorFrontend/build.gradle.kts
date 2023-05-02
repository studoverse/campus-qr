plugins {
  kotlin("js")

  // Workaround for https://youtrack.jetbrains.com/issue/KT-51921 respectively https://github.com/JetBrains/kotlin-wrappers/issues/1077
  id("io.github.turansky.kfc.legacy-union") version "6.15.0" // TODO: @mh Remove when using IR compiler because otherwise compilation fails
}

val ktor_version: String = "2.2.3" // 2.3.0 requires new IR compiler
val kotlinx_html_version: String = "0.8.0"
val kotlinx_serialization_version: String = "1.4.1" // 1.5.0 requires new IR compiler
val kotlinx_coroutines_version: String = "1.6.4"

repositories {
  mavenCentral()
}

fun kotlinw(target: String): String = "org.jetbrains.kotlin-wrappers:kotlin-$target"
val kotlinWrappersVersion = "1.0.0-pre.523"

kotlin {
  js(LEGACY) {
    useCommonJs()
    browser()
    binaries.executable()
  }
}

// Fixes webpack-cli incompatibility by pinning the newest version.
rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
  versions.webpackCli.version = "5.0.2"
}

dependencies {
  implementation(kotlin("stdlib-js"))
  implementation(project(":common"))
  implementation(enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))

  api("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")
  api(kotlinw("react"))
  api(kotlinw("emotion"))
  api(kotlinw("react-dom"))
  api(kotlinw("extensions"))
  api(kotlinw("mui"))
  api(kotlinw("mui-icons"))
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
  api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
  api("io.ktor:ktor-client-content-negotiation:$ktor_version")

  // kotlinx-serialization + Ktor client
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
  implementation("io.ktor:ktor-client-core:$ktor_version")
  implementation("io.ktor:ktor-client-serialization:$ktor_version")

  implementation(npm("normalize.css", "8.0.1"))
  api(devNpm("style-loader", "3.3.1"))
  api(devNpm("css-loader", "6.7.1"))

  api(npm("react", "18.2.0"))
  api(npm("react-dom", "18.2.0"))

  api(npm("@mui/material", "5.9.1"))
  api(npm("@mui/icons-material", "5.10.9"))

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