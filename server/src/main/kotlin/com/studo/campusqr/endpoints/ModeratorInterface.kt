package com.studo.campusqr.endpoints

import com.studo.campusqr.extensions.getResourceAsStream
import com.studo.campusqr.extensions.language
import com.studo.campusqr.localDebug
import com.studo.campusqr.utils.getCsrfToken
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.date.*
import kotlinx.html.*

/**
 * This file contains everything we need to serve the moderation frontend.
 */

suspend fun ApplicationCall.returnModeratorIndexHtml() {
  response.headers.append("Cache-Control", "no-cache") // Don't cache because of CSRF token
  addLanguageCookieIfNeeded(language = this.language)
  val csrfToken = getCsrfToken()
  respondHtml(HttpStatusCode.OK) {
    moderatorIndex(this@returnModeratorIndexHtml, csrfToken)
  }
}

suspend fun ApplicationCall.returnModeratorJs() =
  respondBytes(moderatorUiJs!!, contentType = ContentType.Text.JavaScript)

// Keep the whole JS in memory to reduce disk IO
private val moderatorUiJs: ByteArray? = run {
  val js = getResourceAsStream("/moderatorFrontend/campusqr-admin.js")
    ?.bufferedReader()
    ?.use { it.readText() }

  js?.toByteArray() // Save as byte array here, so ktor doesn't need any further transformation to stream to client.
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
      src = when {
        localDebug -> "/moderatorFrontend.js"
        else -> "/admin/campusqr-admin.js"
      }
    }
  }
}