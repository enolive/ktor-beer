package de.welcz

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
data class Beer(
  @SerialName("_id")
  @Serializable(with = ObjectIdSerializer::class)
  val id: ObjectId? = null,
  val brand: String,
  val name: String,
  val strength: Double,
)

@Serializable
data class PartialBeer(
  val brand: String,
  val name: String,
  val strength: Double,
) {
  fun withId(id: ObjectId?) = Beer(id, brand, name, strength)
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