package com.studo.campusqr.common

enum class LoginResult {
  LOGIN_FAILED,
  LOGIN_SUCCESSFUL,
  UNKNOWN_ERROR // In case the server returned e.g. a maintenance page
}

enum class UserPermission {
  EDIT_USERS, // Can add or delete users
  EDIT_OWN_ACCESS, // Can give access to existing locations (including guest check-in)
  EDIT_ALL_ACCESS, // Can give access to existing locations and manage all access of other users (including guest check-in)
  EDIT_LOCATIONS, // Can add or delete locations
  VIEW_CHECKINS, // Can access check-in data
}

val emailSeparators = arrayOf(" ", ",", ";")

enum class LocationAccessType {
  FREE, RESTRICTED
}