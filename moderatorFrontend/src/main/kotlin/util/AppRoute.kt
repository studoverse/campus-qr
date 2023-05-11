package util

import app.allUrls
import web.location.Location
import web.url.URL
import webcore.extensions.decodeURIComponent
import webcore.extensions.encodeURIComponent

class AppRoute(
  val url: MbUrl,
  val pathParams: Map<String, String> = emptyMap(),
  val queryParams: Map<String, String> = emptyMap()
) {
  // Url with path and query params
  val relativeUrl: String = run {
    val urlWithParams = url.path
      .split("/")
      .joinToString("/") { pathComponent ->
        if (pathComponent.startsWith(":")) {
          pathParams[pathComponent.removePrefix(":")] ?: error("$pathComponent was missing from path")
        } else {
          pathComponent
        }
      }

    val queryString = if (queryParams.isNotEmpty()) {
      "?" + queryParams.map { (key, value) -> "${encodeURIComponent(key)}=${encodeURIComponent(value)}" }
        .joinToString("&")
    } else ""

    urlWithParams + queryString
  }

  override fun equals(other: Any?): Boolean = this === other || (other as? AppRoute)?.relativeUrl == this.relativeUrl
  override fun hashCode(): Int = relativeUrl.hashCode()
}

/**
 * Convert Url string to AppRoute.
 *
 * Add additional pathParams and queryParams here, to avoid doing string operations and
 * manual encoding when constructing a route with any params.
 *
 * Query params of [this] are encoded, therefore the get decoded in toRoute(). queryParams and pathParams need no encoding.
 */
fun String.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap(),
): AppRoute? {
  val splitPath = this.substringBefore("#").substringBefore("?").split("/")
  // Find the matching view
  val matchingView = allUrls.find {
    val pattern = it.path.split("/")
    (pattern.size == splitPath.size) && (splitPath zip pattern).all { (pathPart, patternPart) ->
      ((pathPart == patternPart) || patternPart.startsWith(":"))
    }
  } ?: return null

  // Construct map of path parameters
  val matchingPattern = matchingView.path.split("/")
  val allPathParams = (splitPath zip matchingPattern)
    .asSequence()
    .filter { (_, patternPart) -> patternPart.startsWith(":") }
    .map { (pathPart, patternPart) -> patternPart.removePrefix(":") to pathPart }
    .toMap() + pathParams

  // Construct a map of query parameters
  val allQueryParams = this.queryParams + queryParams

  return AppRoute(matchingView, allPathParams, allQueryParams)
}

val URL.relativeUrl: String get() = href.removePrefix(origin).substringBefore("#")

val Location.relativeUrl: String get() = href.removePrefix(origin).substringBefore("#")

val String.queryParams: Map<String, String>
  get() = this
    .substringBefore("#")
    .substringAfter("?", missingDelimiterValue = "")
    .split("&")
    .map { it.split("=") } // List<String> -> List<List<String>>
    .mapNotNull { it.takeIf { it.count() == 2 } } // Only take key-value params
    .map { (key, value) -> decodeURIComponent(key) to decodeURIComponent(value) } // List<String> -> key - value
    .toMap()
