package com.studo.campusqr.endpoints

import com.studo.campusqr.baseUrl
import com.studo.campusqr.common.LoginResult
import com.studo.campusqr.common.UserData
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.Configuration
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

private suspend fun getUser(id: String?): BackendUser? {
  if (id == null) return null
  return runOnDb { getCollection<BackendUser>().findOne(BackendUser::_id equal id) }
}

suspend fun AuthenticatedApplicationCall.getUserData() {
  val appName = runOnDb {
    getCollection<Configuration>().findOne(Configuration::_id equal "appName")?.stringValue ?: ""
  }

  respondObject(UserData().apply {
    this.appName = appName
    this.clientUser = user.toClientClass(this@getUserData.language)
  })
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

  val params = receiveJsonMap()
  val email = params["email"] ?: run {
    respondEnum(LoginResult.LOGIN_FAILED)
    return
  }
  val password = params["password"] ?: run {
    respondEnum(LoginResult.LOGIN_FAILED)
    return
  }

  val user = runOnDb { getCollection<BackendUser>().findOne(BackendUser::email equal email) }

  if (user == null || !Algorithm.validatePassword(password, user.passwordHash)) {
    respondEnum(LoginResult.LOGIN_FAILED)
    return
  }

  runOnDb {
    getCollection<BackendUser>().updateOne(BackendUser::_id equal user._id, BackendUser::firstLoginDate equal null) {
      BackendUser::firstLoginDate setTo Date()
    }
  }

  val sessionToken = (getSessionToken() ?: createNewSessionToken()).apply {
    userId = user._id
  }

  runOnDb {
    getCollection<SessionToken>().insertOne(sessionToken, upsert = true)
  }

  respondEnum(LoginResult.LOGIN_SUCCESSFUL)
}