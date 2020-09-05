package com.studo.campusqr.auth

import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.Configuration
import com.studo.campusqr.extensions.runOnDb
import com.studo.katerbase.equal

interface AuthProvider {
  sealed class Result {
    object InvalidCredentials : Result()
    class Success(val user: BackendUser) : Result()
  }

  suspend fun init()
  suspend fun login(email: String, password: String): Result
}

suspend fun getAuthProvider(): AuthProvider {
  val ldapUrl = runOnDb {
    getCollection<Configuration>().findOne(Configuration::_id equal "ldapUrl")?.stringValue
  }

  return if (!ldapUrl.isNullOrEmpty()) {
    // Authentication via LDAP
    LdapAuth(ldapUrl)
  } else {
    // Authentication via Campus QR
    CampusQrAuth()
  }.apply {
    init()
  }
}