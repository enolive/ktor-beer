package de.welcz

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.bson.conversions.Bson
import org.bson.types.ObjectId

interface BeerService {
  suspend fun create(beer: PartialBeer): Beer
  fun findAll(): Flow<Beer>
  suspend fun findById(id: String): Either<RequestError, Beer>
  suspend fun update(id: String, beer: PartialBeer): Either<RequestError, Beer>
  suspend fun deleteById(id: String): Either<RequestError, Unit>
}

class MongoBeerService(mongoDatabase: MongoDatabase) : BeerService {
  private var collection: MongoCollection<Beer> = mongoDatabase.getCollection("beers")

  override suspend fun create(beer: PartialBeer): Beer {
    val toCreate = beer.withId(null)
    val result = collection.insertOne(toCreate)
    val id = result.insertedId!!.asObjectId().value
    return beer.withId(id)
  }

  override fun findAll(): Flow<Beer> = collection.find()

  override suspend fun findById(id: String) = either {
    val objectId = id.toObjectId().bind()
    val found = collection
      .find(filterById(objectId))
      .firstOrNull()
    ensureNotNull(found) { ResourceNotFound(id) }
  }

  override suspend fun deleteById(id: String) = either {
    val objectId = id.toObjectId().bind()
    val deleteResult = collection.deleteOne(Filters.eq("_id", objectId))
    ensure(deleteResult.deletedCount == 1L) { ResourceNotFound(id) }
  }

  override suspend fun update(id: String, beer: PartialBeer) = either {
    val objectId = id.toObjectId().bind()
    val toUpdate = beer.withId(objectId)
    val updateResult = collection.replaceOne(filterById(objectId), toUpdate)
    ensure(updateResult.matchedCount == 1L) { ResourceNotFound(id) }
    toUpdate
  }
}

fun filterById(id: ObjectId): Bson = Filters.eq("_id", id)

private val logger = KtorSimpleLogger("Storage")

private fun String.toObjectId() = Either.catchOrThrow<IllegalArgumentException, ObjectId> {
  ObjectId(this)
}
  .onLeft { logger.error("creating an object id failed", it) }
  .mapLeft { MalformedId(this) }

/**
 * Establishes connection with a MongoDB database.
 *
 * The following configuration properties (in application.yaml/application.conf) can be specified:
 * * `db.mongo.user` username for your database
 * * `db.mongo.password` password for the user
 * * `db.mongo.host` host that will be used for the database connection
 * * `db.mongo.port` port that will be used for the database connection
 * * `db.mongo.maxPoolSize` maximum number of connections to a MongoDB server
 * * `db.mongo.database.name` name of the database
 *
 * IMPORTANT NOTE: in order to make MongoDB connection working, you have to start a MongoDB server first.
 * See the instructions here: https://www.mongodb.com/docs/manual/administration/install-community/
 * all the paramaters above
 *
 * @returns [MongoDatabase] instance
 * */
fun Application.connectToMongoDB(): MongoDatabase {
  val user = environment.config.tryGetString("db.mongo.user")
  val password = environment.config.tryGetString("db.mongo.password")
  val host = environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
  val port = environment.config.tryGetString("db.mongo.port") ?: "27017"
  val maxPoolSize = environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20
  val databaseName = environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"

  val credentials = user?.let { userVal -> password?.let { passwordVal -> "$userVal:$passwordVal@" } }.orEmpty()
  val uri = "mongodb://$credentials$host:$port/?maxPoolSize=$maxPoolSize&w=majority"

  val mongoClient = MongoClient.create(uri)
  val database = mongoClient.getDatabase(databaseName)

  monitor.subscribe(ApplicationStopped) {
    mongoClient.close()
  }

  return database
}
