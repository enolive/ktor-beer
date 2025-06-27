package de.welcz.domain

import arrow.core.Either
import de.welcz.Beer
import de.welcz.PartialBeer
import kotlinx.coroutines.flow.Flow

interface BeerService {
  suspend fun create(beer: PartialBeer): Beer
  fun findAll(): Flow<Beer>
  suspend fun findById(id: String): Either<RequestError, Beer>
  suspend fun update(id: String, beer: PartialBeer): Either<RequestError, Beer>
  suspend fun deleteById(id: String): Either<RequestError, Unit>
}