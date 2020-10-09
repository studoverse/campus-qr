package com.studo.campusqr.auth

import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.extensions.runOnDb

interface AuthProvider {
  sealed class Result {
    object InvalidCredentials : Result()
    class Success(val user: BackendUser) : Result()
  }

  suspend fun init()
  suspend fun login(email: String, password: String): Result
}

suspend fun getAuthProvider(): AuthProvider {
  val ldapUrl: String = runOnDb { getConfig("ldapUrl") }

  return when {
    // Authentication via LDAP
    ldapUrl.isNotEmpty() -> LdapAuth(ldapUrl)

    // Authentication via Campus QR
    else -> CampusQrAuth()
  }.apply { init() }
}