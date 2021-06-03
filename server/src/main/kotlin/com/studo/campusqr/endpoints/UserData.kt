package com.studo.campusqr.endpoints

import com.moshbit.katerbase.equal
import com.studo.campusqr.auth.AuthProvider
import com.studo.campusqr.auth.CampusQrAuth
import com.studo.campusqr.authProvider
import com.studo.campusqr.baseUrl
import com.studo.campusqr.common.LoginResult
import com.studo.campusqr.common.UserData
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.SessionToken
import com.studo.campusqr.extensions.*
import com.studo.campusqr.localDebug
import com.studo.campusqr.utils.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.sessions.*
import java.util.*

/**
 * This file contains every endpoint which is used in the user lifecycle.
 */

suspend fun ApplicationCall.getUserData() {
  val appName: String = runOnDb { getConfig("appName") }
  val liveCheckInsViewEnabled = runOnDb { getConfig<Boolean>("liveCheckInsViewEnabled") }

  // sessionToken or user might be null when user is logged out or session expired
  val sessionToken = getSessionToken()
  val user = sessionToken?.let { runOnDb { getUser(it) } }

  // Make sure not to cache logged-in state, so the browser will re-fetch this in any case e.g. when pressing back in browser after logout
  // see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control#preventing_caching
  response.header("Cache-Control", "no-store, max-age=0")

  respondObject(
    UserData(
      appName = appName,
      clientUser = user?.toClientClass(this@getUserData.language),
      externalAuthProvider = authProvider !is CampusQrAuth,
      liveCheckInsViewEnabled = liveCheckInsViewEnabled,
    )
  )
}

suspend fun AuthenticatedApplicationCall.logout() {
  runOnDb {
    getCollection<SessionToken>().updateOne(SessionToken::_id equal sessionToken._id) {
      SessionToken::expiryDate setTo Date()
    }
  }
  sessions.clear<Session>()

  // Reload page, so we get a new session and csrf token
  if (localDebug && this.request.headers["host"] == "localhost:8072") {
    respondRedirect("http://localhost:8072/") // Custom URL for dev-webserver
  } else {
    respondRedirect(baseUrl, permanent = false)
  }
}

suspend fun ApplicationCall.login() {
  validateCsrfToken()

  val params = receiveJsonStringMap()
  val email = params["email"]?.trim()?.toLowerCase() ?: run {
    respondEnum(LoginResult.LOGIN_FAILED)
    return
  }
  val password = params["password"] ?: run {
    respondEnum(LoginResult.LOGIN_FAILED)
    return
  }

  val loginResult = authProvider.login(email, password)

  if (loginResult is AuthProvider.Result.InvalidCredentials) {
    respondEnum(LoginResult.LOGIN_FAILED)
    return
  }

  // User login was successful
  loginResult as AuthProvider.Result.Success

  runOnDb {
    getCollection<BackendUser>().updateOne(
      BackendUser::_id equal loginResult.user._id,
      BackendUser::firstLoginDate equal null
    ) {
      BackendUser::firstLoginDate setTo Date()
    }
  }

  val sessionToken = (getSessionToken() ?: createNewSessionToken()).apply {
    // Security precaution
    if (userId != null && userId != loginResult.user._id) throw IllegalStateException("Wanted to update existing session for another user")

    // Set/Upgrade session token to logged in session
    userId = loginResult.user._id
  }

  runOnDb {
    getCollection<SessionToken>().insertOne(sessionToken, upsert = true)
  }

  respondEnum(LoginResult.LOGIN_SUCCESSFUL)
}