package com.studo.campusqr

import ch.qos.logback.classic.Level
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.automaticDataDeletion
import com.studo.campusqr.database.initialDatabaseSetup
import com.studo.campusqr.endpoints.*
import com.studo.campusqr.utils.Session
import com.studo.campusqr.utils.getAuthenticatedCall
import com.studo.katerbase.setLogLevel
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.net.URL
import java.time.Duration

object Server

val serverScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
val baseUrl: String get() = MainDatabase.getConfig("baseUrl")

suspend fun main() {
  initialDatabaseSetup()

  automaticDataDeletion()

  setLogLevel("io.netty", Level.INFO)

  embeddedServer(
    Netty,
    port = System.getenv("PORT")?.toIntOrNull() ?: 8070,
    host = System.getenv("HOST") ?: "0.0.0.0"
  ) {
    install(DefaultHeaders) {
      header("X-Frame-Options", "DENY")
      header("Strict-Transport-Security", "max-age=31536000; preload")
      header("X-Content-Type-Options", "nosniff")
      header("Content-Security-Policy", "script-src 'self'")
      header("Referrer-Policy", "no-referrer")
      header(
          "Feature-Policy", "accelerometer 'none'; camera 'none'; geolocation 'none'; gyroscope 'none'; " +
          "magnetometer 'none'; microphone 'none'; payment 'none'; usb 'none'"
      )
    }
    install(StatusPages) {
      exception<Throwable> { cause ->
        println(cause) // For easier error debugging print all errors that happen on the server side
        throw cause
      }
    }
    install(Compression) {
      default()
    }
    install(Sessions) {
      cookie<Session>("SESSION_CAMPUS_QR")
    }

    install(CORS) {
      host(URL(baseUrl).host, schemes = listOf("https", "http"))
      allowNonSimpleContentTypes = true
      allowCredentials = true
      maxAgeInSeconds = Duration.ofDays(1).toSeconds()
    }

    routing {
      route("favicon.ico") {
        handle {
          call.response.status(HttpStatusCode.NotFound)
          call.respond(HttpStatusCode.NotFound.description)
        }
      }
      get("/") { call.index() }
      get("campus-qr") { call.userFrontend() }
      route("user") {
        get("data") { call.getAuthenticatedCall()?.getUserData() }
        get("logout") { call.getAuthenticatedCall()?.logout() }
        post("login") { call.login() }

        // Management
        post("create") { call.getAuthenticatedCall()?.createNewUser() }
        post("delete") { call.getAuthenticatedCall()?.deleteUser() }
        post("edit") { call.getAuthenticatedCall()?.editUser() }
        get("list") { call.getAuthenticatedCall()?.listUsers() }
      }
      route("location") {
        post("create") { call.getAuthenticatedCall()?.createLocation() }
        route("list") {
          get { call.getAuthenticatedCall()?.listLocations() }
          get("qr-codes") { call.getAuthenticatedCall()?.viewAllQrCodes() }
        }

        route("{id}") {
          post("visit") { call.visitLocation() }
          get("visitsCsv") { call.getAuthenticatedCall()?.returnLocationVisitCsvData() }
          post("edit") { call.getAuthenticatedCall()?.editLocation() }
          get("qr-code") { call.getAuthenticatedCall()?.viewSingleQrCode() }
          get("delete") { call.getAuthenticatedCall()?.deleteLocation() }
        }
      }
      route("access") {
        post("list") { call.getAuthenticatedCall()?.listAccess() }
        post("create") { call.getAuthenticatedCall()?.createAccess() }

        route("{id}") {
          get("/") { call.getAuthenticatedCall()?.getAccess() }
          get("delete") { call.getAuthenticatedCall()?.deleteAccess() }
          get("duplicate") { call.getAuthenticatedCall()?.duplicateAccess() }
          post("edit") { call.getAuthenticatedCall()?.editAccess() }
        }
      }
      post("report/list") { call.getAuthenticatedCall()?.returnReportData() }
      route("admin") {
        get("campusqr-admin.js") { call.returnModeratorJs() }
        get("/{...}") { call.returnModeratorIndexHtml() }
      }
      static("/static") {
        resources()
      }
    }
  }.start(wait = true)
}