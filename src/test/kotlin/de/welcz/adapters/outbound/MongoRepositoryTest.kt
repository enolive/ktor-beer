package de.welcz.adapters.outbound

import com.mongodb.kotlin.client.coroutine.MongoClient
import de.welcz.domain.BeerService
import de.welcz.domain.MalformedId
import de.welcz.domain.ResourceNotFound
import de.welcz.testutil.TestData.randomObjectId
import de.welcz.testutil.TestData.randomPartialBeer
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoRepositoryTest : DescribeSpec({
  val mongoContainer = MongoDBContainer(DockerImageName.parse("mongo:7.0"))
  extension(mongoContainer.perSpec())
  lateinit var underTest: BeerService

  beforeEach {
    val connectionString = mongoContainer.connectionString
    val mongoClient = MongoClient.create(connectionString)
    underTest = MongoBeerRepository(mongoClient.getDatabase("test"))
  }

  afterEach {
    underTest.clearAllForTesting()
  }

  describe("creating and finding") {
    it("creates beer and finds it by id") {
      val partialBeer = randomPartialBeer()

      val createdBeer = underTest.create(partialBeer)
      createdBeer.brand shouldBe partialBeer.brand

      val foundBeer = underTest.findById(createdBeer.id.toString())
      foundBeer shouldBeRight createdBeer
    }

    it("finds all created beers") {
      val beer1 = randomPartialBeer()
      val beer2 = randomPartialBeer()

      underTest.create(beer1)
      underTest.create(beer2)

      val allBeers = underTest.findAll().toList()

      allBeers.size shouldBe 2
      allBeers.map { it.brand } shouldContainExactlyInAnyOrder listOf(beer1.brand, beer2.brand)
      allBeers.map { it.name } shouldContainExactlyInAnyOrder listOf(beer1.name, beer2.name)
      allBeers.map { it.strength } shouldContainExactlyInAnyOrder listOf(beer1.strength, beer2.strength)
    }
  }

  describe("updating") {
    it("updates existing beer") {
      val originalBeer = randomPartialBeer()
      val updatedBeerData = randomPartialBeer()
      val createdBeer = underTest.create(originalBeer)

      val updateResult = underTest.update(createdBeer.id.toString(), updatedBeerData)

      with(updateResult.shouldBeRight()) {
        this.id shouldBe createdBeer.id
        this.brand shouldBe updatedBeerData.brand
        this.name shouldBe updatedBeerData.name
        this.strength shouldBe updatedBeerData.strength
      }
    }

    it("returns error when updating non-existent beer") {
      val nonExistentId = randomObjectId().toString()
      val updateData = randomPartialBeer()

      val result = underTest.update(nonExistentId, updateData)

      result shouldBeLeft ResourceNotFound(nonExistentId)
    }
  }

  describe("deleting") {
    it("deletes existing beer") {
      val partialBeer = randomPartialBeer()
      val createdBeer = underTest.create(partialBeer)

      val deleteResult = underTest.deleteById(createdBeer.id.toString())
      deleteResult shouldBeRight Unit

      // Verify beer is actually deleted
      val findResult = underTest.findById(createdBeer.id.toString())
      findResult shouldBeLeft ResourceNotFound(createdBeer.id.toString())
    }

    it("returns error when deleting non-existent beer") {
      val nonExistentId = randomObjectId().toString()

      val result = underTest.deleteById(nonExistentId)

      result shouldBeLeft ResourceNotFound(nonExistentId)
    }
  }

  describe("error handling") {
    it("returns error when finding beer with invalid id") {
      val invalidId = "invalid-id"

      val result = underTest.findById(invalidId)

      result shouldBeLeft MalformedId(invalidId)
    }

    it("returns error when updating beer with invalid id") {
      val invalidId = "invalid-id"
      val updateData = randomPartialBeer()

      val result = underTest.update(invalidId, updateData)

      result shouldBeLeft MalformedId(invalidId)
    }

    it("returns error when deleting beer with invalid id") {
      val invalidId = "invalid-id"

      val result = underTest.deleteById(invalidId)

      result shouldBeLeft MalformedId(invalidId)
    }
  }
})

private suspend fun BeerService.clearAllForTesting() {
  if (this is MongoBeerRepository) {
    // Access the database to clear all beers
    this.collection.deleteMany(Document())
  }
}