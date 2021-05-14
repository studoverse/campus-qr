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
    ldapUrl.isNotEmpty() -> LdapAuth(ldapUrl) // Authentication via LDAP
    else -> CampusQrAuth() // Authentication via Campus QR
  }.apply { init() }
}