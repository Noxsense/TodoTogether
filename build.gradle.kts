buildscript {
  repositories {
    gradlePluginPortal()

    // Gradle 4.1 and higher include support for Google's Maven repo using
    // the google() method. And you need to include this repo to download
    // Android Gradle plugin 3.0.0 or higher.
    google()
    mavenCentral()
  }

  dependencies {
    // needed for plugins { kotlin("android") ... }
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.21")
    classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.5.21")

    classpath("com.android.tools.build:gradle:4.2.0")
  }
}

repositories {
  mavenCentral()
}

allprojects {
  group = "de.nox"
  version = "0.0.0"

  repositories {
    google()
    mavenCentral()
  }
}

// Extra definition how to clean.
tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
