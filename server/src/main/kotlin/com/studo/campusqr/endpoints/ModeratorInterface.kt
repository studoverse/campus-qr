package com.studo.campusqr.endpoints

import com.studo.campusqr.extensions.filterValuesNotNull
import com.studo.campusqr.extensions.getDirectoryContentPath
import com.studo.campusqr.extensions.getResourceAsStream
import com.studo.campusqr.extensions.language
import com.studo.campusqr.localDebug
import com.studo.campusqr.utils.getCsrfToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.*
import io.ktor.util.date.*
import kotlinx.html.*

/**
 * This file contains everything we need to serve the moderation frontend.
 */

@Target(AnnotationTarget.PROPERTY)
@RequiresOptIn("Use getCachedResource to access resources.")
annotation class InternalResourceUsage

suspend fun ApplicationCall.returnModeratorIndexHtml() {
  response.headers.append("Cache-Control", "no-cache") // Don't cache because of CSRF token
  addLanguageCookieIfNeeded(language = this.language)
  val csrfToken = getCsrfToken()
  respondHtml(HttpStatusCode.OK) {
    moderatorIndex(this@returnModeratorIndexHtml, csrfToken)
  }
}

@InternalResourceUsage
private val projectResources: Map<String, ByteArray> by lazy {
  fun getCampusQrResource(uri: String): ByteArray? {
    return getResourceAsStream(uri)
      ?.bufferedReader()
      ?.use { it.readText() } // Changes only on re-deployment
      ?.toByteArray()
  }

  getDirectoryContentPath("/moderatorFrontend")!!
    .filter { it.endsWith(".js") || it.endsWith(".js.map") }
    .associate { path ->
      path.removePrefix("/moderatorFrontend/") to getCampusQrResource(path)
    }.filterValuesNotNull()
}

@OptIn(InternalResourceUsage::class)
private fun getCachedResource(uri: String): ByteArray {
  return projectResources[uri] ?: throw NotFoundException("Resource not found: $uri")
}

private suspend fun ApplicationCall.respondResource() {
  val relativePath = parameters.getAll("jsFileName")!!.joinToString("/")
  respondBytes(
    bytes = getCachedResource(relativePath),
    contentType = ContentType.Text.JavaScript
  )
}

suspend fun ApplicationCall.returnModeratorJs() {
  respondResource()
}

private fun ApplicationCall.addLanguageCookieIfNeeded(language: String) {
  // Set a language cookie if not already set
  if (request.cookies["MbLang"] == null) {
    response.cookies.append(
      name = "MbLang",
      value = language,
      expires = GMTDate(59, 59, 23, 31, Month.DECEMBER, 9999), // Don't expire
      path = "/"
    )
  }
}

private fun HTML.moderatorIndex(call: ApplicationCall, csrfToken: String) {
  lang = call.language
  head {
    meta {
      charset = "utf-8"
    }
    meta {
      name = "viewport"
      content = "width=device-width, initial-scale=1, shrink-to-fit=no"
    }
    title {
      +"Campus QR"
    }
    meta {
      name = "csrfToken"
      content = csrfToken
    }
    link {
      rel = "stylesheet"
      href = "https://fonts.googleapis.com/css?family=Roboto:300,400,500"
    }
    link {
      rel = "icon"
      type = "image/png"
      sizes = "64x64"
      href = "/static/images/favicon.png"
    }
  }
  body {
    noScript {
      +"You need to enable JavaScript to run this app."
    }
    div {
      id = "root"
    }
    script {
      type = "module"
      src = when {
        localDebug -> "/moderatorFrontend.js"
        else -> "/admin/js/campusqr-admin.js"
      }
    }
  }
}