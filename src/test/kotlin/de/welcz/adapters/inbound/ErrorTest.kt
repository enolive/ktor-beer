package de.welcz.adapters.inbound

import arrow.core.left
import arrow.core.raise.either
import de.welcz.domain.MalformedId
import de.welcz.domain.ResourceNotFound
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldBeEmpty
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.intellij.lang.annotations.Language

class ErrorTest : DescribeSpec({
  describe("error handling") {
    it("handles MalformedId error with correct status and content") {
      testApplication {
        setupApplication()

        val response = client.get("/malformed-id/12345")

        response.shouldHaveStatus(HttpStatusCode.BadRequest)
        @Language("json") val expectedBody =
          """
            {
              "message": "The id is invalid",
              "id": "12345"
            }
          """.trimIndent()
        response.bodyAsText().shouldEqualJson {
          fieldComparison = FieldComparison.Lenient
          expectedBody
        }
      }
    }

    it("handles ResourceNotFound error with correct status and content") {
      testApplication {
        setupApplication()

        val response = client.get("/resource-not-found/abc123")

        response.shouldHaveStatus(HttpStatusCode.NoContent)
        response.bodyAsText().shouldBeEmpty()
      }
    }

    it("handles InvalidBody error with correct status and content") {
      testApplication {
        setupApplication()

        val response = client.post("/invalid-body") {
          contentType(ContentType.Application.Json)
          setBody("invalid json content")
        }

        response.shouldHaveStatus(HttpStatusCode.BadRequest)
        @Language("json") val expectedBody =
          """
            {
              "message": "Request body is invalid"
            }
          """.trimIndent()
        response.bodyAsText().shouldEqualJson {
          fieldComparison = FieldComparison.Lenient
          expectedBody
        }
      }
    }
  }
})

private fun ApplicationTestBuilder.setupApplication() {
  application {
    install(ContentNegotiation) {
      json()
    }
    routing {
      get("malformed-id/{id}") {
        val id = call.parameters["id"]!!
        MalformedId(id).left().respondEither()
      }

      get("resource-not-found/{id}") {
        val id = call.parameters["id"]!!
        ResourceNotFound(id).left().respondEither()
      }

      post("invalid-body") {
        either {
          call.receiveBeer().bind()
        }.respondEither()
      }
    }
  }
}