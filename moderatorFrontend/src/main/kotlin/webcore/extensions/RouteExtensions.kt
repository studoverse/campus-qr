package webcore.extensions

import util.*
import web.location.Location
import web.location.location
import web.url.URL

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

fun MbUrl.toRouteWithExistingQueryParams(): AppRoute? = toRoute(queryParams = location.href.queryParams)