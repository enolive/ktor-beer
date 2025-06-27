package de.welcz.adapters.inbound

import arrow.core.Either
import de.welcz.PartialBeer
import de.welcz.domain.InvalidBody
import de.welcz.domain.MalformedId
import de.welcz.domain.RequestError
import de.welcz.domain.ResourceNotFound
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*

fun RoutingCall.receiveId(): String = parameters["id"]!!

suspend fun RoutingCall.receiveBeer() =
  Either
    .catch { receive<PartialBeer>() }
    .onLeft { logger.error("creating a beer from the request body failed", it) }
    .mapLeft { InvalidBody() }

private val logger = KtorSimpleLogger("BeersRoutes")

context(ctx: RoutingContext)
suspend inline fun <reified T : Any> Either<RequestError, T>.respondEither(
  statusCode: HttpStatusCode = HttpStatusCode.Companion.OK,
  builder: ResponseHeaders.(T) -> Unit = { },
) {
  val logger = KtorSimpleLogger("Response")
  val call = ctx.call
  this.fold(
    { error ->
      logger.warn(error.toString())
      val (status, shouldHaveBody) = when (error) {
        is MalformedId, is InvalidBody -> HttpStatusCode.Companion.BadRequest to true
        is ResourceNotFound -> HttpStatusCode.Companion.NoContent to false
      }
      call.response.status(status)
      if (shouldHaveBody) {
        call.respond(error)
      }
    },
    { value ->
      builder(call.response.headers, value)
      call.respond<T>(statusCode, value)
    }
  )
}