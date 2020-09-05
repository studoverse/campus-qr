package com.studo.campusqr.auth

import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.utils.Algorithm
import com.studo.katerbase.equal

class CampusQrAuth : AuthProvider {
  override suspend fun init() {
  }

  override suspend fun login(email: String, password: String): AuthProvider.Result {
    val user = runOnDb { getCollection<BackendUser>().findOne(BackendUser::email equal email) }

    return if (user == null || !Algorithm.validatePassword(password, user.passwordHash!!)) {
      AuthProvider.Result.InvalidCredentials
    } else {
      AuthProvider.Result.Success(user)
    }
  }

}