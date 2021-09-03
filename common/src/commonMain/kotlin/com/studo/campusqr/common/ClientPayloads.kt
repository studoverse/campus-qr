package com.studo.campusqr.common

import kotlinx.serialization.Serializable

interface ClientPayload

@Serializable
class ClientLocation(
  val id: String,
  val name: String,
  var checkInCount: Int?,
  val accessType: LocationAccessType,
  val seatCount: Int?,
) : ClientPayload

@Serializable
class UserData(
  var appName: String,
  var clientUser: ClientUser? = null, // Null when unauthenticated
  var externalAuthProvider: Boolean = false,
  var liveCheckInsViewEnabled: Boolean,
) : ClientPayload

val UserData.isAuthenticated get() = clientUser != null

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