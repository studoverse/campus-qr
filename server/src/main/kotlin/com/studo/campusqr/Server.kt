package com.studo.campusqr

import ch.qos.logback.classic.Level
import com.studo.campusqr.auth.AuthProvider
import com.studo.campusqr.auth.getAuthProvider
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.initialDatabaseSetup
import com.studo.campusqr.database.startAutomaticCheckOut
import com.studo.campusqr.database.startAutomaticDataDeletion
import com.studo.campusqr.endpoints.*
import com.studo.campusqr.utils.Session
import com.studo.campusqr.utils.getAuthenticatedCall
import com.studo.campusqr.utils.setLogLevel
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.net.URL
import java.time.Duration

object Server

val serverScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
val baseUrl: String get() = MainDatabase.getConfig("baseUrl")
val localDebug: Boolean = System.getenv("DEBUG") == "true"
lateinit var authProvider: AuthProvider

suspend fun main() {
  initialDatabaseSetup()

  authProvider = getAuthProvider()

  startAutomaticDataDeletion()
  startAutomaticCheckOut()

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
      if (!localDebug) {
        header("Content-Security-Policy", "script-src 'self'")
      }
      header("Referrer-Policy", "no-referrer")
      header(
        "Feature-Policy", "accelerometer 'none'; camera 'none'; geolocation 'none'; gyroscope 'none'; " +
            "magnetometer 'none'; microphone 'none'; payment 'none'; usb 'none'"
      )
    }
    if (System.getenv("ENABLE_CALL_LOGGING") == "true") {
      setLogLevel("ktor.application", Level.TRACE)
      install(CallLogging)
    }
    install(StatusPages) {
      exception<Throwable> { _, cause ->
        println(cause) // For easier error debugging print all errors that happen on the server side
        throw cause
      }
    }
    install(Compression) {
      default()
    }
    install(IgnoreTrailingSlash)
    install(Sessions) {
      val key = hex(MainDatabase.getConfig<String>("sessionHmacSecret"))
      cookie<Session>("SESSION_CAMPUS_QR") {
        transform(SessionTransportTransformerMessageAuthentication(key))
      }
    }

    install(CORS) {
      allowHost(URL(baseUrl).host, schemes = listOf("https", "http"))
      if (localDebug) {
        allowHost("localhost:8072")
      }
      allowNonSimpleContentTypes = true
      allowCredentials = true
      maxAgeInSeconds = Duration.ofDays(1).seconds
    }

    routing {
      get("favicon.ico") { call.favicon() }
      get("robots.txt") { call.robotsTxt() }
      get("/") { call.index() }
      route("campus-qr") {
        get { call.userFrontend() }
        get("checkout") { call.checkOutView() }
        get("liveCheckIns") { call.liveCheckInsView() }
      }
      route("user") {
        get("data") { call.getUserData() }
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
        route("qr-codes") {
          get { call.getAuthenticatedCall()?.viewAllQrCodes() }
          get("checkout") { call.getAuthenticatedCall()?.viewCheckoutCode() }
        }
        get("list") { call.getAuthenticatedCall()?.listLocations() }

        route("{id}") {
          post("visit") { call.visitLocation() }
          post("guestCheckIn") { call.getAuthenticatedCall()?.guestCheckIn() }
          post("checkout") { call.checkOutLocation() }
          get("visitsCsv") { call.getAuthenticatedCall()?.returnLocationVisitCsvData() }
          post("edit") { call.getAuthenticatedCall()?.editLocation() }
          get("qr-code") { call.getAuthenticatedCall()?.viewSingleQrCode() }
          post("delete") { call.getAuthenticatedCall()?.deleteLocation() }
          post("editSeatFilter") { call.getAuthenticatedCall()?.editSeatFilter() }
          post("deleteSeatFilter") { call.getAuthenticatedCall()?.deleteSeatFilter() }
          get("getLiveCheckIns") { call.getLiveCheckIns() }
        }
      }
      route("access") {
        get("list") { call.getAuthenticatedCall()?.listAccess() }
        get("export") { call.getAuthenticatedCall()?.listExportAccess() }
        post("create") { call.getAuthenticatedCall()?.createAccess() }

        route("{id}") {
          get("/") { call.getAuthenticatedCall()?.getAccess() }
          post("delete") { call.getAuthenticatedCall()?.deleteAccess() }
          post("duplicate") { call.getAuthenticatedCall()?.duplicateAccess() }
          post("edit") { call.getAuthenticatedCall()?.editAccess() }
        }
      }
      route("report") {
        post("list") { call.getAuthenticatedCall()?.returnReportData() }
        post("listActiveCheckIns") { call.getAuthenticatedCall()?.listAllActiveCheckIns() }
        get("listActiveGuestCheckIns") { call.getAuthenticatedCall()?.listGuestActiveCheckIns() }
      }
      route("admin") {
        get("campusqr-admin.js") { call.returnModeratorJs() }
        get("/{...}") { call.returnModeratorIndexHtml() }
      }
      staticResources(remotePath = "/static", basePackage = null)
    }
  }.start(wait = true)
}
