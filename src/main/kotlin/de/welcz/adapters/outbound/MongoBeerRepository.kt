package de.welcz.adapters.outbound

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.welcz.Beer
import de.welcz.PartialBeer
import de.welcz.domain.BeerService
import de.welcz.domain.MalformedId
import de.welcz.domain.ResourceNotFound
import io.ktor.util.logging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.bson.conversions.Bson
import org.bson.types.ObjectId

class MongoBeerRepository(mongoDatabase: MongoDatabase) : BeerService {
  internal var collection: MongoCollection<StoredBeer> = mongoDatabase.getCollection("beers")

  override suspend fun create(beer: PartialBeer): Beer {
    val toCreate = StoredBeer.fromPartial(beer)
    val result = collection.insertOne(toCreate)
    val id = result.insertedId!!.asObjectId().value
    return beer.toBeer(id.toHexString())
  }

  override fun findAll(): Flow<Beer> = collection.find().map { it.toBeer() }

  override suspend fun findById(id: String) = either {
    val objectId = id.toObjectId().bind()
    val found = collection
      .find(filterById(objectId))
      .firstOrNull()
    ensureNotNull(found) { ResourceNotFound(id) }
    found.toBeer()
  }

  override suspend fun deleteById(id: String) = either {
    val objectId = id.toObjectId().bind()
    val deleteResult = collection.deleteOne(Filters.eq("_id", objectId))
    ensure(deleteResult.deletedCount == 1L) { ResourceNotFound(id) }
  }

  override suspend fun update(id: String, beer: PartialBeer) = either {
    val objectId = id.toObjectId().bind()
    val toUpdate = StoredBeer.fromPartial(beer, objectId)
    val updateResult = collection.replaceOne(filterById(objectId), toUpdate)
    ensure(updateResult.matchedCount == 1L) { ResourceNotFound(id) }
    beer.toBeer(id)
  }
}

fun filterById(id: ObjectId): Bson = Filters.eq("_id", id)

private val logger = KtorSimpleLogger("Storage")

private fun String.toObjectId() = Either.catchOrThrow<IllegalArgumentException, ObjectId> {
  ObjectId(this)
}
  .onLeft { logger.error("creating an object id failed", it) }
  .mapLeft { MalformedId(this) }

