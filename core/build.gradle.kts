plugins {
  id("java-library")

  // kotlin("plugin-jpa") //  version "1.5.30"

  // Apply the Kotlin JVM plugin to add support for Kotlin.
  kotlin("jvm")
}

dependencies {
  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  // Use the Kotlin JDK 8 standard library.
  // implementation(kotlin("kotlin-reflect"))
  implementation(kotlin("stdlib-jdk8"))

  // jackson (json library)
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0-rc2")

  // in-memory database (for testing and development)
  // runtimeOnly("com.h2database:h2")

  // -- Tests ---

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  // Use the Kotlin JUnit integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
