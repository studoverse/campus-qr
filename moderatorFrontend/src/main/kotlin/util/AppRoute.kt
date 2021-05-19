package util

import org.w3c.dom.Location
import org.w3c.dom.url.URL
import webcore.extensions.decodeURIComponent
import webcore.extensions.encodeURIComponent

class AppRoute(
  val url: Url,
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
private fun String.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap()
): AppRoute? {
  val splitPath = this.substringBefore("?").split("/")
  // Find the matching view
  val matchingView = Url.values().find {
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
  val allQueryParams = this
    .substringAfter("?", missingDelimiterValue = "")
    .split("&")
    .map { it.split("=") } // List<String> -> List<List<String>>
    .mapNotNull { it.takeIf { it.count() == 2 } } // Only take key-value params
    .associate { (key, value) -> decodeURIComponent(key) to decodeURIComponent(value) } + queryParams

  return AppRoute(matchingView, allPathParams, allQueryParams)
}

val URL.relativeUrl: String get() = href.removePrefix(origin).substringBefore("#")

/**
 * Add additional pathParams and queryParams here, to avoid doing string operations and
 * manual encoding when constructing a route with any params.
 *
 * Query params of URL are encoded, therefore the get decoded in toRoute(). queryParams and pathParams need no encoding.
 */
fun URL.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap()
): AppRoute? = relativeUrl.toRoute(pathParams, queryParams)

val Location.relativeUrl: String get() = href.removePrefix(origin).substringBefore("#")

/**
 * Add additional pathParams and queryParams here, to avoid doing string operations and
 * manual encoding when constructing a route with any params.
 *
 * Query params of Location are encoded, therefore the get decoded in toRoute(). queryParams and pathParams need no encoding.
 */
fun Location.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap()
): AppRoute? = relativeUrl.toRoute(pathParams, queryParams)

/**
 * Add additional pathParams and queryParams here, to avoid doing string operations and
 * manual encoding when constructing a route with any params.
 *
 * Query params of Url are encoded, therefore the get decoded in toRoute(). queryParams and pathParams need no encoding.
 */
fun Url.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap()
): AppRoute? = path.toRoute(pathParams, queryParams)