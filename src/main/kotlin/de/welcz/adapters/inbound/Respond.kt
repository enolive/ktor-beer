package de.welcz.adapters.inbound

import arrow.core.Either
import de.welcz.domain.InvalidBody
import de.welcz.domain.MalformedId
import de.welcz.domain.RequestError
import de.welcz.domain.ResourceNotFound
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*

context(ctx: RoutingContext)
suspend inline fun <reified T : Any> Either<RequestError, T>.respond(
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