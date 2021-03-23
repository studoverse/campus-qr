package com.studo.campusqr.endpoints

import com.moshbit.katerbase.MongoDatabase
import com.moshbit.katerbase.MongoMainEntry
import com.moshbit.katerbase.equal
import com.studo.campusqr.common.EditUserData
import com.studo.campusqr.common.NewUserData
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.database.BackendUser
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.SessionToken
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.Algorithm
import com.studo.campusqr.utils.AuthenticatedApplicationCall

/**
 * This file contains every endpoint which is used in the user management.
 */
suspend fun AuthenticatedApplicationCall.createNewUser() {
  if (!sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  if (!user.canEditUsers) {
    respondForbidden()
    return
  }

  val params: NewUserData = receiveClientPayload()

  val email = params.email.trim().toLowerCase()
  val newUser = BackendUser(
    userId = MongoMainEntry.generateId(email), // Use email as primary key. Email can not be changed.
    email = email,
    name = params.name.trim(),
    permissions = params.permissions.map { UserPermission.valueOf(it) }.toSet()
  ).apply {
    this.passwordHash = Algorithm.hashPassword(params.password)
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
  if (!user.canEditUsers) {
    respondForbidden()
    return
  }

  val params = receiveJsonStringMap()
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

  val params: EditUserData = receiveClientPayload()
  val changedUserId = params.userId ?: user._id

  val newName = params.name?.trim()
  val newPassword = params.password
  val newPermissions = params.permissions?.map { UserPermission.valueOf(it) }?.toSet()

  // Only EDIT_USERS users can change the password of other users
  // Only EDIT_USERS users can change user permissions
  if (!user.canEditUsers && (changedUserId != user._id || newPermissions != null)) {
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
      if (newPermissions != null) {
        BackendUser::permissions setTo newPermissions
      }
    }
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.listUsers() {
  if (!user.canEditUsers) {
    respondForbidden()
    return
  }

  val users = runOnDb {
    getCollection<BackendUser>().find().toList()
  }

  respondObject(users.map { it.toClientClass(this.language) })
}