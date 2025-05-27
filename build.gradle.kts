val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val mongo_version: String by project

plugins {
  kotlin("jvm") version "2.1.10"
  id("io.ktor.plugin") version "3.1.3"
  id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
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
  implementation("io.insert-koin:koin-ktor:$koin_version")
  implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
  implementation("io.ktor:ktor-server-content-negotiation")
  implementation("io.ktor:ktor-serialization-kotlinx-json")
  implementation("org.mongodb:mongodb-driver-core:$mongo_version")
  implementation("org.mongodb:mongodb-driver-sync:$mongo_version")
  implementation("org.mongodb:bson:$mongo_version")
  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("io.ktor:ktor-server-config-yaml")
  testImplementation("io.ktor:ktor-server-test-host")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
