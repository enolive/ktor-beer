package de.welcz

import de.welcz.adapters.inbound.configureBeerRoutes
import de.welcz.adapters.inbound.configureOpenApi
import de.welcz.adapters.outbound.MongoBeerRepository
import de.welcz.adapters.outbound.connectToMongoDB
import de.welcz.domain.BeerService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
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
        MongoBeerRepository(connectToMongoDB())
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
