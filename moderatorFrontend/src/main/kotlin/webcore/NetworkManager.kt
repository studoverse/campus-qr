package webcore

import kotlinext.js.getOwnPropertyNames
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import webcore.extensions.await
import webcore.extensions.awaitOrNull
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.reflect.KClass

object NetworkManager {

  suspend inline fun <reified T : Any> get(url: String, urlParams: Json? = null): T? = get(url, urlParams, T::class)

  suspend fun <T : Any> get(url: String, urlParams: Json?, kClass: KClass<T>): T? {
    val urlWithParams = url + (urlParams?.let { "?" + URLSearchParams(urlParams) } ?: "")
    return window.fetch(urlWithParams).parseResponse(kClass)
  }

  suspend inline fun <reified T : Any> post(url: String, params: Json? = null, headers: Json? = null): T? =
    post(url, params, T::class, headers)

  suspend inline fun <reified T : Any> post(url: String, json: String? = null, headers: Json? = null): T? =
    post(url, json, T::class, headers)

  suspend fun <T : Any> post(url: String, params: Json? = null, kClass: KClass<T>, headers: Json? = null): T? {
    return post(url, JSON.stringify(params), kClass, headers)
  }

  suspend fun <T : Any> post(url: String, json: String? = null, kClass: KClass<T>, headers: Json? = null): T? {
    val response = window.fetch(url, RequestInit().also { request ->
      request.method = "POST"
      json?.let { request.body = json }
      headers?.let { request.headers = it }
    })
    return response.parseResponse(kClass)
  }

  suspend inline fun <reified T : Any> put(url: String, params: Json? = null): T? = put(url, params, T::class)

  suspend fun <T : Any> put(url: String, params: Json? = null, kClass: KClass<T>): T? {
    val response = window.fetch(url, RequestInit().also { request ->
      request.method = "PUT"
      params?.let { request.body = JSON.stringify(it) }
    })
    return response.parseResponse(kClass)
  }

  // Holds a unique hash for the local frontend version
  // The server send a hash with every call, when this hash mismatches with the server version, the page is reloaded.
  private var localFrontendVersionHash: String? = null

  private fun Response.reloadIfLocalVersionIsOutdated() {
    // If server doesn't send the hash, this feature is not supported on the backend
    val remoteVersionHash = headers.get("x-version-hash") ?: return

    // Set the local version hash on the first call, the check for any changes on the subsequent calls
    when {
      localFrontendVersionHash == null -> localFrontendVersionHash = remoteVersionHash
      localFrontendVersionHash != remoteVersionHash -> {
        console.log("Reloading page because a newer version is available!")
        window.location.reload()
      }
    }
  }

  private suspend inline fun <T : Any> Promise<Response>.parseResponse(kClass: KClass<T>): T? {
    return try {
      val response: Response = this.awaitOrNull() ?: return null
      response.reloadIfLocalVersionIsOutdated()
      @Suppress("UNCHECKED_CAST")
      when (kClass) {
        String::class -> response.toStringMessage() as T
        Map::class -> response.toMapMessage() as T
        else -> response.toJsonMessage<T>()
      }
    } catch (e: dynamic) {
      console.log("Failed to parse response", this)
      null
    }
  }

  private suspend fun <T> Response.toJsonMessage(): T? {
    return if (status == 200.toShort()) {
      this.json().await().asDynamic() as T
    } else {
      null
    }
  }

  private suspend fun Response.toMapMessage(): Map<String, Any?>? {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val json = if (status == 200.toShort()) {
      this.json().await().asDynamic() as Json
    } else {
      return null
    }
    return mutableMapOf<String, Any?>()
      .apply { json.getOwnPropertyNames().forEach { key -> this[key] = json[key] } }
  }

  private suspend fun Response.toStringMessage(): String? {
    return if (status == 200.toShort()) {
      text().await()
    } else {
      null
    }
  }
}