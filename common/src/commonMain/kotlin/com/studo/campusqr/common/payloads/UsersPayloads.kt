package com.studo.campusqr.common.payloads

import com.studo.campusqr.common.UserPermission
import kotlinx.serialization.Serializable

@Serializable
class NewUserData(
  val email: String,
  val name: String,
  val password: String,
  val permissions: List<String>,
) : ClientPayload

@Serializable
class EditUserData(
  val userId: String? = null,
  val name: String?,
  val password: String?,
  val permissions: List<String>?,
) : ClientPayload

@Serializable
class DeleteUserData(
  val userId: String
) : ClientPayload

@Serializable
class ClientUser(
  val id: String,
  val email: String,
  val name: String,
  val permissions: Set<UserPermission>,
  val firstLoginDate: String,
) : ClientPayload

// Keep in sync with BackendUser
val ClientUser.canEditUsers get() = UserPermission.EDIT_USERS in permissions
val ClientUser.canEditLocations get() = UserPermission.EDIT_LOCATIONS in permissions
val ClientUser.canViewCheckIns get() = UserPermission.VIEW_CHECKINS in permissions
val ClientUser.canEditAnyLocationAccess get() = canEditOwnLocationAccess || canEditAllLocationAccess
val ClientUser.canEditOwnLocationAccess get() = UserPermission.EDIT_OWN_ACCESS in permissions
val ClientUser.canEditAllLocationAccess get() = UserPermission.EDIT_ALL_ACCESS in permissions