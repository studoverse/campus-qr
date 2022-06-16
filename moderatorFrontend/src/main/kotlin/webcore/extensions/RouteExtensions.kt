package webcore.extensions

import kotlinx.browser.window
import org.w3c.dom.Location
import org.w3c.dom.url.URL
import util.*

/**
 * Add additional pathParams and queryParams here, to avoid doing string operations and
 * manual encoding when constructing a route with any params.
 *
 * Query params of URL are encoded, therefore the get decoded in toRoute(). queryParams and pathParams need no encoding.
 */
fun URL.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap(),
): AppRoute? = relativeUrl.toRoute(MbUrl.urls, pathParams, queryParams)

/**
 * Add additional pathParams and queryParams here, to avoid doing string operations and
 * manual encoding when constructing a route with any params.
 *
 * Query params of Location are encoded, therefore the get decoded in toRoute(). queryParams and pathParams need no encoding.
 */
fun Location.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap(),
): AppRoute? = relativeUrl.toRoute(MbUrl.urls, pathParams, queryParams)

/**
 * Add additional pathParams and queryParams here, to avoid doing string operations and
 * manual encoding when constructing a route with any params.
 *
 * Query params of Url are encoded, therefore the get decoded in toRoute(). queryParams and pathParams need no encoding.
 */
fun MbUrl.toRoute(
  pathParams: Map<String, String> = emptyMap(),
  queryParams: Map<String, String> = emptyMap(),
): AppRoute? = path.toRoute(MbUrl.urls, pathParams, queryParams)

fun MbUrl.toRouteWithExistingQueryParams(): AppRoute? = toRoute(queryParams = window.location.href.queryParams)