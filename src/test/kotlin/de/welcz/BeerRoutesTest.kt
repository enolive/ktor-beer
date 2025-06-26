package de.welcz

import arrow.core.left
import arrow.core.right
import de.welcz.testutil.TestData.randomBeer
import de.welcz.testutil.TestData.randomBeers
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.coroutines.flow.asFlow
import org.bson.types.ObjectId
import org.intellij.lang.annotations.Language
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class BeerRoutesTest : DescribeSpec({
  describe("API for /beers") {
    beforeAny {
      clearAllMocks()
    }

    describe("has GET /") {
      it("returns list of existing beers") {
        withBeerRoutes {
          val existingBeers = randomBeers(5)
          every { serviceMock.findAll() } returns existingBeers.asFlow()

          val response = client.get("/beers")

          response shouldHaveStatus HttpStatusCode.OK
          val expectedJson = existingBeers.joinToString(prefix = "[", postfix = "]") { it.toExpectedJson() }
          response.bodyAsText() shouldEqualJson expectedJson
        }
      }
    }

    describe("has GET /{id}") {
      it("returns existing beer") {
        withBeerRoutes {
          val theId = "existing-id"
          val existingBeer = Beer(ObjectId.get(), "Astra", "Urhell", 5.0)
          coEvery { serviceMock.findById(theId) } returns existingBeer.right()

          val response = client.get("/beers/$theId")

          response shouldHaveStatus HttpStatusCode.OK
          response.bodyAsText() shouldEqualJson existingBeer.toExpectedJson()
        }
      }

      it("returns no content if beer does not exist") {
        withBeerRoutes {
          val theId = "not-existing-id"
          coEvery { serviceMock.findById(theId) } returns ResourceNotFound(theId).left()

          val response = client.get("/beers/$theId")

          response shouldHaveStatus HttpStatusCode.NoContent
        }
      }
    }

    describe("has DELETE /{id}") {
      it("deletes existing beer") {
        withBeerRoutes {
          val theId = "existing-id"
          coEvery { serviceMock.deleteById(any()) } returns Unit.right()

          val response = client.delete("/beers/$theId")

          response shouldHaveStatus HttpStatusCode.NoContent
          coVerify { serviceMock.deleteById(theId) }
        }
      }

      it("ignores deleting not existing beer") {
        withBeerRoutes {
          val theId = "not-existing-id"
          coEvery { serviceMock.deleteById(any()) } returns ResourceNotFound(theId).left()

          val response = client.delete("/beers/$theId")

          response shouldHaveStatus HttpStatusCode.NoContent
          coVerify { serviceMock.deleteById(theId) }
        }
      }
    }

    describe("has POST /") {
      it("creates a new beer") {
        withBeerRoutes {
          @Language("json") val beerToCreate = """
              {
                "brand": "Astra",
                "name": "Urhell",
                "strength": 5.0
              }
            """.trimIndent()
          val expectedToCreate = PartialBeer("Astra", "Urhell", 5.0)
          val createdBeer = randomBeer()
          val capturedBeer = slot<PartialBeer>()
          coEvery { serviceMock.create(capture(capturedBeer)) } returns createdBeer

          val response = client.post("/beers") {
            contentType(ContentType.Application.Json)
            setBody(beerToCreate)
          }

          response shouldHaveStatus HttpStatusCode.Created
          response.bodyAsText() shouldEqualJson createdBeer.toExpectedJson()
          capturedBeer.captured shouldBe expectedToCreate
        }
      }
    }

    describe("has PUT /{id}") {
      it("updates existing beer") {
        withBeerRoutes {
          val theId = "existing-id"
          @Language("json") val toUpdate = """
              {
                "brand": "Astra",
                "name": "Urhell",
                "strength": 5.0
              }
            """.trimIndent()
          val expectedToUpdate = PartialBeer("Astra", "Urhell", 5.0)
          val updatedBeer = randomBeer()
          val captureUpdate = slot<PartialBeer>()
          coEvery { serviceMock.update(theId, capture(captureUpdate)) } returns updatedBeer.right()

          val response = client.put("/beers/$theId") {
            contentType(ContentType.Application.Json)
            setBody(toUpdate)
          }

          response shouldHaveStatus HttpStatusCode.OK
          response.bodyAsText() shouldEqualJson updatedBeer.toExpectedJson()
          captureUpdate.captured shouldBe expectedToUpdate
        }
      }

      it("returns no content if beer does not exist") {
        withBeerRoutes {
          val theId = "not existing-id"
          @Language("json") val toUpdate = """
              {
                "brand": "Astra",
                "name": "Urhell",
                "strength": 5.0
              }
            """.trimIndent()
          coEvery { serviceMock.update(theId, any()) } returns ResourceNotFound(theId).left()

          val response = client.put("/beers/$theId") {
            contentType(ContentType.Application.Json)
            setBody(toUpdate)
          }

          response shouldHaveStatus HttpStatusCode.NoContent
        }
      }
    }
  }

})

@Language("JSON")
private fun Beer.toExpectedJson() = """
  {
    "_id": "$id",
    "brand": "$brand",
    "name": "$name",
    "strength": $strength
  }
""".trimIndent()

val serviceMock = mockk<BeerService>()
fun withBeerRoutes(block: suspend ApplicationTestBuilder.() -> Unit) {
  testApplication {
    application {
      install(Koin) {
        this.modules(
          module {
            single<BeerService> {
              serviceMock
            }
          }
        )
      }
      install(ContentNegotiation) {
        json()
      }
      configureBeerRoutes()
    }
    block()
  }
}