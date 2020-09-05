package com.studo.campusqr.endpoints

import com.studo.campusqr.common.UserType
import com.studo.campusqr.common.UserType.ADMIN
import com.studo.campusqr.common.UserType.MODERATOR
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.Algorithm
import com.studo.campusqr.utils.getSessionToken
import com.studo.campusqr.utils.getUser
import com.studo.campusqr.utils.isAuthenticated
import com.studo.katerbase.MongoDatabase
import com.studo.katerbase.MongoMainEntry
import com.studo.katerbase.equal
import io.ktor.application.*

/**
 * This file contains every endpoint which is used in the user management.
 */
suspend fun ApplicationCall.createNewUser() {
  if (!getSessionToken().isAuthenticated) {
    respondForbidden()
    return
  }

  val currentUser = getUser()

  if (currentUser.type != ADMIN) {
    respondForbidden()
    return
  }

  val params = receiveJsonMap()

  val email = params.getValue("email").trim()
  val newUser = BackendUser(
    userId = MongoMainEntry.generateId(email), // Use email as primary key. Email can not be changed.
    email = email,
    name = params.getValue("name").trim(),
    type = params["userType"]?.let { UserType.valueOf(it) } ?: MODERATOR
  ).apply {
    this.passwordHash = Algorithm.hashPassword(params.getValue("password"))
    this.createdBy = currentUser._id
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

suspend fun ApplicationCall.deleteUser() {
  if (!getSessionToken().isAuthenticated || getUser().type != ADMIN) {
    respondForbidden()
    return
  }

  val params = receiveJsonMap()
  val userId = params.getValue("userId")

  runOnDb {
    getCollection<BackendUser>().deleteOne(BackendUser::_id equal userId)
  }

  respondOk()
}

suspend fun ApplicationCall.editUser() {
  if (!getSessionToken().isAuthenticated) {
    respondForbidden()
    return
  }
  val currentUser = getUser()

  val params = receiveJsonMap()
  val changedUserId = params["userId"] ?: currentUser._id

  val newName = params["name"]?.trim()
  val newPassword = params["password"]
  val newUserType = params["userType"]?.let { UserType.valueOf(it) }

  // Only ADMIN users can change the password of other users
  if (currentUser.type != ADMIN && changedUserId != currentUser._id) {
    respondForbidden()
    return
  }
  // A MODERATOR user can't change types
  if (currentUser.type == MODERATOR && newUserType != null) {
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

suspend fun ApplicationCall.listUsers() {
  if (!getSessionToken().isAuthenticated || getUser().type != ADMIN) {
    respondForbidden()
    return
  }

  val users = runOnDb {
    getCollection<BackendUser>().find().toList()
  }

  respondObject(users.map { it.toClientClass(this.language) })
}