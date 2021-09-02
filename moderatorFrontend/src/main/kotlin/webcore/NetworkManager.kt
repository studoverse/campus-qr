package webcore

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.browser.window
import kotlinx.serialization.json.*

/**
 * NetworkManager uses Ktor client with kotlinx.serialization to create network requests.
 * All functions in this object check the "x-version-hash" header and reload the application if necessary.
 */
object NetworkManager {
  val client = HttpClient {
    install(JsonFeature) {
      serializer = KotlinxSerializer()
    }
    defaultRequest {
      if (this.url.host == "localhost" && window.location.hostname == "localhost") {
        port = window.location.port.toInt()
      }
    }
  }

  suspend inline fun <reified T : Any> get(
    url: String,
    urlParams: Map<String, Any?> = emptyMap(),
    headers: Map<String, Any?> = emptyMap(),
  ): T? = try {
    val response: HttpResponse = client.get(url) {
      urlParams.forEach { parameter(it.key, it.value) }
      headers.forEach { header(it.key, it.value) }
    }
    response.reloadIfLocalVersionIsOutdated()
    response.receive<T>()
  } catch (e: Exception) {
    console.log("get", e)
    null
  }

  /**
   * [body] Map<String, Any?>, List<Any?> or @Serializable
   */
  suspend inline fun <reified T : Any> post(
    url: String,
    body: Any? = null,
    urlParams: Map<String, Any?> = emptyMap(),
    headers: Map<String, Any?> = emptyMap(),
  ): T? = try {
    val response: HttpResponse = client.post(url) {
      body?.let {
        contentType(ContentType.Application.Json)
        this.body = when (body) {
          // Kotlinx's serialization doesn't support serialization of collections with mixed types,
          // see https://youtrack.jetbrains.com/issue/KTOR-3063.
          // So handle maps and lists with our toJsonElement()
          is Map<*, *> -> body.toJsonElement()
          is List<*> -> body.toJsonElement()
          else -> body // Let the Ktor client handle @Serializable classes.
        }
      }
      urlParams.forEach { parameter(it.key, it.value) }
      headers.forEach { header(it.key, it.value) }
    }
    response.reloadIfLocalVersionIsOutdated()
    response.receive<T>()
  } catch (e: Exception) {
    console.log("post", e)
    null
  }

  suspend inline fun <reified T : Any> put(
    url: String,
    urlParams: Map<String, Any?> = emptyMap(),
    headers: Map<String, Any?> = emptyMap(),
  ): T? = try {
    val response: HttpResponse = client.put(url) {
      urlParams.forEach { parameter(it.key, it.value) }
      headers.forEach { header(it.key, it.value) }
    }
    response.reloadIfLocalVersionIsOutdated()
    response.receive<T>()
  } catch (e: Exception) {
    console.log("put", e)
    null
  }

  // Holds a unique hash for the local frontend version
  // The server send a hash with every call, when this hash mismatches with the server version, the page is reloaded.
  private var localFrontendVersionHash: String? = null

  fun HttpResponse.reloadIfLocalVersionIsOutdated() {
    // If server doesn't send the hash, this feature is not supported on the backend
    val remoteVersionHash = headers.flattenEntries().toMap()["x-version-hash"] ?: return
    // Set the local version hash on the first call, the check for any changes on the subsequent calls
    when {
      localFrontendVersionHash == null -> localFrontendVersionHash = remoteVersionHash
      localFrontendVersionHash != remoteVersionHash -> {
        console.log("Reloading page because a newer version is available!")
        window.location.reload()
      }
    }
  }

  fun List<*>.toJsonElement(): JsonElement {
    val list: MutableList<JsonElement> = mutableListOf()
    this.forEach { value ->
      when (value) {
        null -> list.add(JsonNull)
        is Map<*, *> -> list.add(value.toJsonElement())
        is List<*> -> list.add(value.toJsonElement())
        is Boolean -> list.add(JsonPrimitive(value))
        is Number -> list.add(JsonPrimitive(value))
        is String -> list.add(JsonPrimitive(value))
        is Enum<*> -> list.add(JsonPrimitive(value.toString()))
        else -> throw IllegalStateException("Can't serialize unknown collection type: $value")
      }
    }
    return JsonArray(list)
  }

  fun Map<*, *>.toJsonElement(): JsonElement {
    val map: MutableMap<String, JsonElement> = mutableMapOf()
    this.forEach { (key, value) ->
      key as String
      when (value) {
        null -> map[key] = JsonNull
        is Map<*, *> -> map[key] = value.toJsonElement()
        is List<*> -> map[key] = value.toJsonElement()
        is Boolean -> map[key] = JsonPrimitive(value)
        is Number -> map[key] = JsonPrimitive(value)
        is String -> map[key] = JsonPrimitive(value)
        is Enum<*> -> map[key] = JsonPrimitive(value.toString())
        else -> throw IllegalStateException("Can't serialize unknown type: $value")
      }
    }
    return JsonObject(map)
  }
}