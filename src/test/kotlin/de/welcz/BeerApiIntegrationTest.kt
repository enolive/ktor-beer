package de.welcz

import de.welcz.testutil.TestData.randomPartialBeer
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class BeerApiIntegrationTest : DescribeSpec({
  val mongoContainer = MongoDBContainer(DockerImageName.parse("mongo:7.0"))
  extension(mongoContainer.perSpec())

  describe("Beer API Integration Tests") {
    it("should retrieve all beers after creating some") {
      testApplication {
        setupTestApplication(mongoContainer)
        val beer1 = randomPartialBeer()
        val beer2 = randomPartialBeer()
        val client = createClient {
          install(ContentNegotiation) {
            json()
          }
        }
        client.post("/beers") {
          contentType(ContentType.Application.Json)
          setBody(beer1)
        }
        client.post("/beers") {
          contentType(ContentType.Application.Json)
          setBody(beer2)
        }

        val response = client.get("/beers")

        response.status shouldBe HttpStatusCode.OK
        val beers = response.body<List<Beer>>()
        beers.map { it.copy(id = null) }.shouldContainExactlyInAnyOrder(
          beer1.toBeer(),
          beer2.toBeer(),
        )
      }
    }
  }
})

private fun ApplicationTestBuilder.setupTestApplication(mongoContainer: MongoDBContainer) {
  environment {
    config = MapApplicationConfig(
      "mongodb.connection" to mongoContainer.connectionString
    )
  }
  application {
    module()
  }
}