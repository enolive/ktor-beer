package de.welcz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Beer(
  @SerialName("_id")
  val id: String? = null,
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
  fun toBeer(id: String? = null) = Beer(id, brand, name, strength)
}

