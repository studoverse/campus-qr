package com.studo.campusqr.endpoints

import com.studo.campusqr.common.UserType
import com.studo.campusqr.common.UserType.*
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.Algorithm
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.katerbase.MongoDatabase
import com.studo.katerbase.equal
import java.util.*

/**
 * This file contains every endpoint which is used in the user management.
 */
suspend fun AuthenticatedApplicationCall.createNewUser() {
  if (sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  if (user.type != ADMIN) {
    respondForbidden()
    return
  }

  val params = receiveJsonMap()

  val newUser = BackendUser().apply {
    email = params.getValue("email").trim()
    _id = generateId(email) // Use email as primary key. Email can not be changed.
    passwordHash = Algorithm.hashPassword(params.getValue("password"))
    name = params.getValue("name").trim()
    createdDate = Date()
    createdBy = user._id
    type = params["userType"]?.let { valueOf(it) } ?: ACCESS_MANAGER
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
  if (!sessionToken.isAuthenticated || user.type != ADMIN) {
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
  if (user.type != ADMIN && (changedUserId != user._id || newUserType != null)) {
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
  if (!sessionToken.isAuthenticated || user.type != ADMIN) {
    respondForbidden()
    return
  }

  val users = runOnDb {
    getCollection<BackendUser>().find().toList()
  }

  respondObject(users.map { it.toClientClass(this.language) })
}