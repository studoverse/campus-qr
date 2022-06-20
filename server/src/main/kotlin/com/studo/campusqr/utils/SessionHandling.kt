package com.studo.campusqr.utils

import com.moshbit.katerbase.equal
import com.moshbit.katerbase.greater
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.SessionToken
import com.studo.campusqr.extensions.addDays
import com.studo.campusqr.extensions.addYears
import com.studo.campusqr.extensions.respondForbidden
import com.studo.campusqr.extensions.runOnDb
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import java.util.*

/**
 * This file contains all helper functions and classes for session handling and authentication.
 */

data class Session(val token: String)

suspend fun ApplicationCall.getAuthenticatedCall(): AuthenticatedApplicationCall? {
  return when (this) {
    is AuthenticatedApplicationCall -> this
    else -> {
      val sessionToken = getSessionToken() ?: run { respondForbidden(); return null }
      val user = getUser(sessionToken) ?: run { respondForbidden(); return null }
      AuthenticatedApplicationCall(this, sessionToken, user)
    }
  }
}

class AuthenticatedApplicationCall(
  private val call: ApplicationCall,
  val sessionToken: SessionToken,
  val user: BackendUser
) : ApplicationCall by call

suspend fun ApplicationCall.createNewSessionToken(): SessionToken {
  val sessionToken = SessionToken().apply {
    val now = Date()

    _id = randomId()
    userId = null // Unauthenticated
    creationDate = now
    expiryDate = now.addYears(1)
  }

  sessions.set(Session(token = sessionToken._id))
  runOnDb {
    getCollection<SessionToken>().insertOne(sessionToken, upsert = false)
  }

  return sessionToken
}

suspend fun ApplicationCall.getSessionToken(): SessionToken? {
  val authorizationHeader = request.headers["X-Authorization"]

  return when {
    // Server-to-server auth via shared secret
    authorizationHeader?.isNotBlank() == true -> when (authorizationHeader) {
      MainDatabase.getConfig<String>("authSharedSecret") -> getOrCreateSharedSecretSessionToken()
      else -> null
    }

    // Default auth with session cookie
    else -> {
      val sessionToken = sessions.get<Session>()?.token?.takeIf { it != sharedSecretSessionId } ?: return null
      runOnDb { getCollection<SessionToken>().findOne(SessionToken::_id equal sessionToken, SessionToken::expiryDate greater Date()) }
    }
  }
}

val SessionToken?.isAuthenticated get() = this?.isAuthenticated ?: false

suspend fun getUser(sessionToken: SessionToken): BackendUser? = when {
  !sessionToken.isAuthenticated -> null
  sessionToken._id == sharedSecretSessionId -> getOrCreateSharedSecretUser()
  else -> runOnDb { getCollection<BackendUser>().findOne(BackendUser::_id equal sessionToken.userId!!) }
}

private const val sharedSecretUserId = "sharedSecretUser"
private const val sharedSecretSessionId = "sharedSecretSessionToken"

private suspend fun getOrCreateSharedSecretSessionToken(): SessionToken = runOnDb {
  getCollection<SessionToken>().findOneOrInsert(SessionToken::_id equal sharedSecretSessionId, SessionToken::expiryDate greater Date()) {
    SessionToken().apply {
      val now = Date()
      creationDate = now
      expiryDate = now.addYears(1000)
      userId = sharedSecretUserId
    }
  }
}

private suspend fun getOrCreateSharedSecretUser(): BackendUser = runOnDb {
  getCollection<BackendUser>().findOneOrInsert(BackendUser::_id equal sharedSecretUserId) {
    BackendUser().apply {
      email = sharedSecretUserId
      createdDate = Date()
      name = "Shared Secret User"
      permissions = UserPermission.values().toSet()
    }
  }
}

private val csrfHashSecret: String = MainDatabase.getConfig("csrfHashSecret")

// csrfToken generation and validation see
// https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#hmac-based-token-pattern
suspend fun ApplicationCall.getCsrfToken(date: Date = Date()): String {
  val sessionToken = getSessionToken() ?: createNewSessionToken()
  return "$csrfHashSecret${sessionToken._id}|${date.time}".sha256() + date.time
}

private val sha265CharLength = "".sha256().length

suspend fun ApplicationCall.validateCsrfToken() {
  val csrfToken = request.headers["csrfToken"] ?: throw SecurityException("No csrfToken header set")

  try {
    // Validate csrfToken age
    val csrfTokenDate = Date(csrfToken.substring(sha265CharLength).toLong())
    val now = Date()
    if (csrfTokenDate > now) {
      throw SecurityException("csrfToken-timestamp > now")
    } else if (csrfTokenDate < now.addDays(-30)) {
      // Token expiry time is 30 days
      throw SecurityException("csrfToken-timestmap too old")
    }

    // Validate csrfToken correctness
    if (getCsrfToken(csrfTokenDate) != csrfToken) {
      throw SecurityException("csrfToken mismatch")
    }

  } catch (e: Exception) {
    // We might also throw if e.g. csrfToken length is not valid or Date constructor fails.
    // Make sure to always throw the same exception here and log the event.
    println("${e.message}")
    throw SecurityException("csrfToken not valid, csrfToken = $csrfToken, sessionToken = ${(getSessionToken() ?: "NO_SESSION_TOKEN")}")
  }
}
