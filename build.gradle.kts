import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val koin_version: String by project
val logback_version: String by project
val mongo_version: String by project
val arrow_version: String by project
val kotest_version: String by project

plugins {
  val kotlin_version = "2.1.20"
  kotlin("jvm") version kotlin_version
  id("io.ktor.plugin") version "3.1.3"
  id("org.jetbrains.kotlin.plugin.serialization") version kotlin_version
  jacoco
}

group = "de.welcz"
version = "0.0.1"

application {
  mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-server-openapi")
  implementation("io.ktor:ktor-server-call-logging")
  implementation("io.ktor:ktor-serialization-kotlinx-json")
  implementation("io.insert-koin:koin-ktor:$koin_version")
  implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
  implementation("io.ktor:ktor-server-content-negotiation")
  implementation("io.ktor:ktor-client-content-negotiation")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")

  implementation(platform("org.mongodb:mongodb-driver-bom:$mongo_version"))
  implementation("org.mongodb:mongodb-driver-kotlin-coroutine")
  implementation("org.mongodb:bson-kotlinx")

  implementation(platform("io.arrow-kt:arrow-stack:$arrow_version"))
  implementation("io.arrow-kt:arrow-core")

  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("io.ktor:ktor-server-config-yaml")
  testImplementation("io.ktor:ktor-server-test-host")
  testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
  testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
  testImplementation("io.kotest:kotest-assertions-json:$kotest_version")
  testImplementation("io.kotest:kotest-property:$kotest_version")
  testImplementation("io.kotest.extensions:kotest-assertions-ktor:2.0.0")
  testImplementation("io.kotest.extensions:kotest-assertions-arrow:2.0.0")
  testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
  testImplementation("io.kotest.extensions:kotest-extensions-koin:1.3.0")
  testImplementation("io.mockk:mockk:1.14.2")
  testImplementation("org.testcontainers:testcontainers:1.21.2")
  testImplementation("org.testcontainers:mongodb:1.19.3")
  testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}

kotlin {
  sourceSets {
    all {
      languageSettings.enableLanguageFeature("ContextParameters")
    }
  }
}

tasks {
  withType<KotlinCompile>().configureEach {
    kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")
  }

  test {
    useJUnitPlatform()
    finalizedBy(jacocoTestReport) // Add this line
  }

  jacocoTestReport {
    dependsOn(test) // Add this block
    reports {
      xml.required = true
      html.required = true
    }
  }

  jacocoTestCoverageVerification {
    violationRules {
      rule {
        limit {
          minimum = "0.80".toBigDecimal() // 80% coverage threshold
        }
      }
    }
  }
}