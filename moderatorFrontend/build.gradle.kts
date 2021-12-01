plugins {
  kotlin("js")
}

val ktor_version: String = "1.6.4"
val kotlinx_html_version: String = "0.7.3"
val kotlinx_serialization_version: String = "1.3.0"
val kotlinx_coroutines_version: String = "1.5.2"

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

// Yarn security fix
// https://blog.jetbrains.com/kotlin/2021/10/control-over-npm-dependencies-in-kotlin-js/
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
  rootProject.the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().disableGranularWorkspaces()
}

tasks.register("backupYarnLock") {
  dependsOn(":kotlinNpmInstall")

  doLast {
    copy {
      from("$rootDir/build/js/yarn.lock")
      rename { "yarn.lock.bak" }
      into(rootDir)
    }
  }

  inputs.file("$rootDir/build/js/yarn.lock").withPropertyName("inputFile")
  outputs.file("$rootDir/yarn.lock.bak").withPropertyName("outputFile")
}

val restoreYarnLock = tasks.register("restoreYarnLock") {
  doLast {
    copy {
      from("$rootDir/yarn.lock.bak")
      rename { "yarn.lock" }
      into("$rootDir/build/js")
    }
  }

  inputs.file("$rootDir/yarn.lock.bak").withPropertyName("inputFile")
  outputs.file("$rootDir/build/js/yarn.lock").withPropertyName("outputFile")
}

rootProject.tasks.named("kotlinNpmInstall").configure {
  dependsOn(restoreYarnLock)
}

tasks.register("validateYarnLock") {
  dependsOn(":kotlinNpmInstall")

  doLast {
    val expected = file("$rootDir/yarn.lock.bak").readText()
    val actual = file("$rootDir/build/js/yarn.lock").readText()

    if (expected != actual) {
      throw AssertionError(
        "Generated yarn.lock differs from the one in the repository. " +
            "It can happen because someone has updated a dependency and haven't run `./gradlew :backupYarnLock --refresh-dependencies` " +
            "afterwards."
      )
    }
  }

  inputs.files("$rootDir/yarn.lock.bak", "$rootDir/build/js/yarn.lock").withPropertyName("inputFiles")
}

// Disable the execution of Yarn’s lifecycle scripts
allprojects {
  rootProject.tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask> {
    args += "--ignore-scripts"
  }
}

///////
// Hack for 50% faster Kotlin/JS builds: Uncomment the `onlyIf { false }` line after first-time project setup.
allprojects {
  rootProject.tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask> {
    // onlyIf { false }
  }
}