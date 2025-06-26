package de.welcz.testutil

import de.welcz.Beer
import de.welcz.PartialBeer
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.bson.types.ObjectId

fun Arb.Companion.objectId(): Arb<ObjectId> =
  arbitrary { ObjectId.get() }

private fun Arb.Companion.name() = Arb.string(3..15, Codepoint.alphanumeric())
private fun Arb.Companion.brand() = Arb.string(5..20, Codepoint.alphanumeric())

fun Arb.Companion.beer(): Arb<Beer> = arbitrary {
  Beer(
    id = Arb.objectId().bind(),
    brand = Arb.brand().bind(),
    name = Arb.name().bind(),
    strength = Arb.double(0.1, 15.0).bind()
  )
}

fun Arb.Companion.partialBeer(): Arb<PartialBeer> = arbitrary {
  PartialBeer(
    brand = Arb.brand().bind(),
    name = Arb.name().bind(),
    strength = Arb.double(0.1, 15.0).bind()
  )
}

object TestData {
  fun randomPartialBeer() = Arb.partialBeer().single()
  fun randomBeer() = Arb.beer().single()
  fun randomBeers(count: Int) = Arb.beer().take(count).toList()
  fun randomObjectId() = Arb.objectId().single()
}