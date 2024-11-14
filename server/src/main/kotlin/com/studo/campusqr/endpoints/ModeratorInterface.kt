package com.studo.campusqr.endpoints

import com.studo.campusqr.extensions.getResourceAsStream
import com.studo.campusqr.extensions.language
import com.studo.campusqr.localDebug
import com.studo.campusqr.utils.getCsrfToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.*
import io.ktor.util.combineSafe
import io.ktor.util.date.*
import kotlinx.html.*
import java.io.File
import java.io.InputStream

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

private fun getCampusQrResource(uri: String): String {
  return getCampusQrResourceAsStream(uri)
    .bufferedReader()
    .use { it.readText() }
}

private fun getCampusQrResourceAsStream(uri: String): InputStream {
  val dir = File("/moderatorFrontend/")
  return getResourceAsStream(dir.combineSafe(uri).path)
    ?: throw NotFoundException(
      "Can't find resource $uri${if (localDebug) " .You need to bundle this for local use!" else ""}"
    )
}

private suspend fun ApplicationCall.respondStream() {
  val relativePath = parameters.getAll("jsFileName")!!.joinToString("/")
  getCampusQrResourceAsStream(relativePath).use {
    respondOutputStream(ContentType.Text.JavaScript) {
      it.copyTo(this)
    }
  }
}

suspend fun ApplicationCall.returnModeratorJs() {
  respondStream()
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