package com.studo.campusqr.endpoints

import com.studo.campusqr.auth.AuthProvider
import com.studo.campusqr.auth.CampusQrAuth
import com.studo.campusqr.authProvider
import com.studo.campusqr.baseUrl
import com.studo.campusqr.common.LoginResult
import com.studo.campusqr.common.UserData
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.SessionToken
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.*
import com.studo.katerbase.equal
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.sessions.*
import java.util.*

/**
 * This file contains every endpoint which is used in the user lifecycle.
 */

suspend fun ApplicationCall.getUserData() {
  val appName: String = runOnDb { getConfig("appName") }

  // sessionToken or user might be null when user is logged out or session expired
  val sessionToken = getSessionToken()
  val user = sessionToken?.let { runOnDb { getUser(it) } }

  respondObject(
    UserData(
      appName = appName,
      clientUser = user?.toClientClass(this@getUserData.language),
      externalAuthProvider = authProvider !is CampusQrAuth,
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
  respondRedirect(baseUrl, permanent = false)
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
    userId = loginResult.user._id
  }

  runOnDb {
    getCollection<SessionToken>().insertOne(sessionToken, upsert = true)
  }

  respondEnum(LoginResult.LOGIN_SUCCESSFUL)
}