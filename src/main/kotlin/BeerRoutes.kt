package de.welcz

import arrow.core.Either
import arrow.core.raise.either
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
      either<InvalidBody, Beer> {
        val partialBeer = call.receiveBeer().bind()
        service.create(partialBeer)
      }.respond(HttpStatusCode.Created)
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
    .mapLeft { InvalidBody }

private val logger = KtorSimpleLogger("BeersRoutes")

// TODO: migrate to context parameters with kotlin 2.2
context(RoutingContext)
private suspend inline fun <reified T : Any> Either<RequestError, T>.respond(statusCode: HttpStatusCode = HttpStatusCode.OK) {
  this.fold(
    { error ->
      logger.warn(error.toString())
      val status = when (error) {
        is MalformedId, InvalidBody -> HttpStatusCode.BadRequest
        is ResourceNotFound -> HttpStatusCode.NoContent
      }
      call.response.status(status)
      call.respond(error)
    },
    { value -> call.respond<T>(statusCode, value) }
  )
}
