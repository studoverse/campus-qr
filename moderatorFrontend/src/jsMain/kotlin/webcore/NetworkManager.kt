package webcore

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import web.location.location

/**
 * NetworkManager uses Ktor client with kotlinx.serialization to create network requests.
 * All functions in this object check the "x-version-hash" header and reload the application if necessary.
 */
object NetworkManager {
  val client = HttpClient {
    install(ContentNegotiation) {
      json(kotlinx.serialization.json.Json {
        classDiscriminator = "@class"
        ignoreUnknownKeys = true
      })
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
    response.body<T>()
  } catch (e: Exception) {
    console.log("get", e)
    null
  }

  suspend inline fun <reified T : Any> post(
    url: String,
    body: Any? = null,
    urlParams: Map<String, Any?> = emptyMap(),
    headers: Map<String, Any?> = emptyMap(),
  ): T? = try {
    val response: HttpResponse = client.post(url) {
      body?.let {
        contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
        setBody(body) // Let the Ktor client handle @Serializable classes.
      }
      urlParams.forEach { parameter(it.key, it.value) }
      headers.forEach { header(it.key, it.value) }
    }
    response.reloadIfLocalVersionIsOutdated()
    response.body<T>()
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
    response.body<T>()
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
        location.reload()
      }
    }
  }
}