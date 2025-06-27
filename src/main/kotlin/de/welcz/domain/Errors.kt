package de.welcz.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class RequestError(val message: String)

@Serializable
data class ResourceNotFound(val id: String) : RequestError("Resource was not found")

@Serializable
data class MalformedId(val id: String) : RequestError("The id is invalid")

@Serializable
class InvalidBody : RequestError("Request body is invalid")