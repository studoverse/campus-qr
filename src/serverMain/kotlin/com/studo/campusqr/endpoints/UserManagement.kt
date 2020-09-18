package com.studo.campusqr.endpoints

import com.studo.campusqr.Server.demoMode
import com.studo.campusqr.common.UserType
import com.studo.campusqr.common.UserType.ACCESS_MANAGER
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.SessionToken
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.Algorithm
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.katerbase.MongoDatabase
import com.studo.katerbase.MongoMainEntry
import com.studo.katerbase.equal

/**
 * This file contains every endpoint which is used in the user management.
 */
suspend fun AuthenticatedApplicationCall.createNewUser() {
  if (!sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  if (!user.isAdmin) {
    respondForbidden()
    return
  }

  val params = receiveJsonMap()

  val email = params.getValue("email").trim()
  val newUser = BackendUser(
      userId = MongoMainEntry.generateId(email), // Use email as primary key. Email can not be changed.
      email = email,
      name = params.getValue("name").trim(),
      type = params["userType"]?.let { UserType.valueOf(it) } ?: ACCESS_MANAGER
  ).apply {
    this.passwordHash = Algorithm.hashPassword(params.getValue("password"))
    this.createdBy = user._id
  }

  try {
    runOnDb {
      getCollection<BackendUser>().insertOne(newUser, upsert = false)
    }
  } catch (e: MongoDatabase.DuplicateKeyException) {
    respondError("already_exists")
    return
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.deleteUser() {
  if (!user.isAdmin) {
    respondForbidden()
    return
  }

  val params = receiveJsonMap()
  val userId = params.getValue("userId")

  runOnDb {
    MainDatabase.getCollection<SessionToken>().deleteMany(SessionToken::userId equal userId)
    getCollection<BackendUser>().deleteOne(BackendUser::_id equal userId)
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.editUser() {
  if (!sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  val params = receiveJsonMap()
  val changedUserId = params["userId"] ?: user._id

  val newName = params["name"]?.trim()
  val newPassword = params["password"]
  val newUserType = params["userType"]?.let { UserType.valueOf(it) }

  // Only ADMIN users can change the password of other users
  // Only ADMIN users can change user types
  // Disallow changing editing user in demo mode
  if ((!user.isAdmin && (changedUserId != user._id || newUserType != null)) || demoMode) {
    respondForbidden()
    return
  }

  runOnDb {
    getCollection<BackendUser>().updateOne(BackendUser::_id equal changedUserId) {
      if (newName != null) {
        BackendUser::name setTo newName
      }
      if (newPassword != null) {
        BackendUser::passwordHash setTo Algorithm.hashPassword(newPassword)
      }
      if (newUserType != null) {
        BackendUser::type setTo newUserType
      }
    }
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.listUsers() {
  if (!user.isAdmin) {
    respondForbidden()
    return
  }

  val users = runOnDb {
    getCollection<BackendUser>().find().toList()
  }

  respondObject(users.map { it.toClientClass(this.language) })
}