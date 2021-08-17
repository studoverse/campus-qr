package webcore

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.browser.window

/**
 * NetworkManager uses Ktor client with kotlinx.serialization to create network requests.
 * All functions in this object check the "x-version-hash" header and reload the application if necessary.
 */
object NetworkManager {

  val client = HttpClient {
    install(JsonFeature) {
      serializer = KotlinxSerializer()
    }
  }

  suspend inline fun <reified T : Any> get(
    url: String,
    urlParams: Map<String, Any?> = emptyMap(),
    headers: Map<String, Any?> = emptyMap(),
  ): T? = try {
    val response: HttpResponse = client.get(url) {
      headers.forEach { header(it.key, it.value) }
      urlParams.forEach { parameter(it.key, it.value) }
    }
    response.reloadIfLocalVersionIsOutdated()
    response.receive<T>()
  } catch (e: Exception) {
    null
  }

  suspend inline fun <reified T : Any> post(
    url: String,
    urlParams: Map<String, Any?> = emptyMap(),
    body: Any? = null,
    headers: Map<String, Any?> = emptyMap(),
  ): T? = try {
    val response: HttpResponse = client.post(url) {
      body?.let {
        contentType(ContentType.Application.Json)
        this.body = body
      }
      headers.forEach { header(it.key, it.value) }
      urlParams.forEach { parameter(it.key, it.value) }
    }
    response.reloadIfLocalVersionIsOutdated()
    response.receive<T>()
  } catch (e: Exception) {
    null
  }

  suspend inline fun <reified T : Any> put(
    url: String,
    urlParams: Map<String, Any?> = emptyMap(),
    headers: Map<String, Any?> = emptyMap(),
  ): T? = try {
    val response: HttpResponse = client.put(url) {
      headers.forEach { header(it.key, it.value) }
      urlParams.forEach { parameter(it.key, it.value) }
    }
    response.reloadIfLocalVersionIsOutdated()
    response.receive<T>()
  } catch (e: Exception) {
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
}