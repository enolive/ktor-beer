package de.welcz.adapters.outbound

import de.welcz.Beer
import de.welcz.PartialBeer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import org.bson.types.ObjectId

@Serializable
data class StoredBeer(
  @SerialName("_id")
  @Serializable(with = ObjectIdSerializer::class)
  val id: ObjectId? = null,
  val brand: String,
  val name: String,
  val strength: Double,
) {
  fun toBeer(): Beer = Beer(
    id = id?.toHexString(),
    brand = brand,
    name = name,
    strength = strength
  )

  companion object {
    fun fromPartial(beer: PartialBeer, id: ObjectId? = null) = StoredBeer(
      brand = beer.brand,
      name = beer.name,
      strength = beer.strength,
      id = id
    )
  }
}

// necessary to make mongodb objectid to work properly with kotlinx serialization
// unlike mentioned in the docs, there is no automatic serialization support for objectid!
@OptIn(ExperimentalSerializationApi::class)
object ObjectIdSerializer : KSerializer<ObjectId> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectId", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ObjectId) {
    when (encoder) {
      is BsonEncoder -> encoder.encodeObjectId(value)
      else -> encoder.encodeString(value.toHexString())
    }
  }

  override fun deserialize(decoder: Decoder): ObjectId = when (decoder) {
    is BsonDecoder -> decoder.decodeObjectId()
    else -> ObjectId(decoder.decodeString())
  }
}