package de.welcz.adapters.outbound

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.logging.*


private val logger = KtorSimpleLogger("Mongo DB Connection")

fun Application.connectToMongoDB(): MongoDatabase {
  val uri =
    environment.config.tryGetString("mongodb.connection")
  requireNotNull(uri) { "MongoDB connection string not found in configuration" }
  logger.info("Connecting to MongoDB: $uri")

  val mongoClient = MongoClient.Factory.create(uri)
  val database = mongoClient.getDatabase("beers")

  monitor.subscribe(ApplicationStopped) {
    mongoClient.close()
  }

  return database
}