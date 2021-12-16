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

kotlin {
  js {
    useCommonJs()
    browser()
  }
}

dependencies {
  implementation(kotlin("stdlib-js"))
  implementation(project(":common"))

  api("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")
  api("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.212-kotlin-1.5.10")
  api("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.212-kotlin-1.5.10")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")

  // kotlinx-serialization + Ktor client
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")
  implementation("io.ktor:ktor-client-core:$ktor_version")
  implementation("io.ktor:ktor-client-serialization:$ktor_version")

  implementation(npm("normalize.css", "8.0.1"))
  implementation(devNpm("style-loader", "2.0.0"))
  implementation(devNpm("css-loader", "5.2.6"))

  api(npm("react", "17.0.2"))
  api(npm("react-dom", "17.0.2"))

  api(npm("@material-ui/core", "4.12.2"))

  // Needed for MuiAutocomplete. Can be removed if it gets merged into @material-ui/core
  api(npm("@material-ui/lab", "4.0.0-alpha.60"))

  api(npm("@material-ui/icons", "4.11.2"))

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