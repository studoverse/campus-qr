plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "11"
    }
  }
  js {
    useCommonJs() // TODO: @mh Test without this line and remove if not needed
    browser()
    //binaries.executable()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
      }
    }
    val commonTest by getting
  }
}
