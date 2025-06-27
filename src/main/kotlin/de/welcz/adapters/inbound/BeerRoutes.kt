package de.welcz.adapters.inbound

import arrow.core.raise.either
import de.welcz.domain.BeerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
      service.findById(id).respondEither(HttpStatusCode.OK)
    }

    delete("/beers/{id}") {
      val id = call.receiveId()
      service
        .deleteById(id)
        .respondEither(HttpStatusCode.NoContent)
    }

    post("/beers") {
      either {
        val partialBeer = call.receiveBeer().bind()
        service.create(partialBeer)
      }.respondEither(HttpStatusCode.Created) {
        append(HttpHeaders.Location, "/beers/${it.id}")
      }
    }

    put("/beers/{id}") {
      val result = either {
        val id = call.receiveId()
        val beer = call.receiveBeer().bind()
        service.update(id, beer).bind()
      }
      result.respondEither()
    }
  }
}


