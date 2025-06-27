package de.welcz.adapters.inbound

import arrow.core.Either
import arrow.core.raise.either
import de.welcz.PartialBeer
import de.welcz.domain.BeerService
import de.welcz.domain.InvalidBody
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.koin.ktor.ext.inject

fun Application.configureBeerRoutes() {
  val service by inject<BeerService>()
  routing {
    get("/beers") {
      val beers = service.findAll()
      call.respond(beers)
    }

    get("/beers/{id}") {
      val id = call.receiveId()
      service.findById(id).respond(HttpStatusCode.OK)
    }

    delete("/beers/{id}") {
      val id = call.receiveId()
      service
        .deleteById(id)
        .respond(HttpStatusCode.NoContent)
    }

    post("/beers") {
      either {
        val partialBeer = call.receiveBeer().bind()
        service.create(partialBeer)
      }.respond(HttpStatusCode.Created) {
        append(HttpHeaders.Location, "/beers/${it.id}")
      }
    }

    put("/beers/{id}") {
      val result = either {
        val id = call.receiveId()
        val beer = call.receiveBeer().bind()
        service.update(id, beer).bind()
      }
      result.respond()
    }
  }
}

private fun RoutingCall.receiveId(): String = parameters["id"]!!

private suspend fun RoutingCall.receiveBeer() =
  Either
    .catchOrThrow<ContentTransformationException, PartialBeer> { receive<PartialBeer>() }
    .onLeft { logger.error("creating a beer from the request body failed", it) }
    .mapLeft { InvalidBody() }

private val logger = KtorSimpleLogger("BeersRoutes")

