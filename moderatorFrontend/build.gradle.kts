plugins {
  kotlin("js")
}

val ktor_version: String = "1.6.7"
val kotlinx_html_version: String = "0.7.3"
val kotlinx_serialization_version: String = "1.3.1"
val kotlinx_coroutines_version: String = "1.5.2"

repositories {
  mavenCentral()
}

fun kotlinw(target: String): String = "org.jetbrains.kotlin-wrappers:kotlin-$target"
val kotlinWrappersVersion = "0.0.1-pre.321-kotlin-1.6.10"

kotlin {
  // TODO: @mh Add incremental compilation when kotlin 1.6.20 is released (https://blog.jetbrains.com/kotlin/2022/02/kotlin-1-6-20-m1-released/)
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
  api(kotlinw("mui"))
  api(kotlinw("mui-icons"))
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")

  implementation(npm("@emotion/react", "11.7.1"))
  implementation(npm("@emotion/styled", "11.6.0"))

  // kotlinx-serialization + Ktor client
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
  implementation("io.ktor:ktor-client-core:$ktor_version")
  implementation("io.ktor:ktor-client-serialization:$ktor_version")

  implementation(npm("normalize.css", "8.0.1"))
  implementation(devNpm("style-loader", "2.0.0"))
  implementation(devNpm("css-loader", "5.2.6"))

  api(npm("react", "17.0.2"))
  api(npm("react-dom", "17.0.2"))

  api(npm("@mui/material", "5.5.2"))

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