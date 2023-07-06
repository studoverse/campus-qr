plugins {
  kotlin("js")
}

val ktor_version: String = "2.3.2"
val kotlinx_html_version: String = "0.9.0"
val kotlinx_serialization_version: String = "1.5.1"
val kotlinx_coroutines_version: String = "1.7.2"

repositories {
  mavenCentral()
}

fun kotlinw(target: String): String = "org.jetbrains.kotlin-wrappers:kotlin-$target"
val kotlinWrappersVersion = "1.0.0-pre.588"

kotlin {
  js(IR) {
    useCommonJs()
    browser()
    binaries.executable()
  }
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
    mainOutputFileName.set("campusqr-admin.js")
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