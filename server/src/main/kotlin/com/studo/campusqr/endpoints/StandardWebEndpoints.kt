package com.studo.campusqr.endpoints

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

/**
 * This file contains endpoints which are related to browser/http standards or conventions.
 */

suspend fun ApplicationCall.favicon() {
  response.status(HttpStatusCode.NotFound)
  respond(HttpStatusCode.NotFound.description)
}

suspend fun ApplicationCall.robotsTxt() {
  // Make sure to not index qr codes dashboard that might be exposed via link
  respondText(
    """
  User-agent: *
  Disallow: /campus-qr
  """.trimIndent(), ContentType.Text.Plain
  )
}