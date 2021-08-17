plugins {
  kotlin("js")
}

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
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

  api("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
  api("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.212-kotlin-1.5.10")
  api("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.212-kotlin-1.5.10")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

  // kotlinx-serialization + Ktor client
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
  implementation("io.ktor:ktor-client-core:1.6.1")
  implementation("io.ktor:ktor-client-serialization:1.6.1")

  implementation(npm("normalize.css", "8.0.1"))
  implementation(devNpm("style-loader", "2.0.0"))
  implementation(devNpm("css-loader", "5.2.6"))

  api(npm("react", "17.0.2"))
  api(npm("react-dom", "17.0.2"))

  api(npm("@material-ui/core", "4.12.2"))

  // Needed for MuiAutocomplete. Can be removed if it gets merged into @material-ui/core
  api(npm("@material-ui/lab", "4.0.0-alpha.60"))

  api(npm("@material-ui/icons", "4.11.2"))

  // Don't update to latest version until @material-ui/pickers v4 comes out
  api(npm("@material-ui/pickers", "3.2.10"))

  api(npm("@date-io/luxon", "1.3.13"))
  api(npm("luxon", "1.24.1"))

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