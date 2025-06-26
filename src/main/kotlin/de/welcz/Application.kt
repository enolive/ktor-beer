package de.welcz

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.module() {
  install(Koin) {
    slf4jLogger()
    modules(module {
      single<BeerService> {
        MongoBeerService(connectToMongoDB())
      }
    })
  }
  install(ContentNegotiation) {
    json()
  }
  install(CallLogging)
  configureBeerRoutes()
  configureOpenApi()
}
