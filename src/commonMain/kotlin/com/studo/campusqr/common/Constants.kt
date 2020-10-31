package com.studo.campusqr.common

enum class LoginResult {
  LOGIN_FAILED,
  LOGIN_SUCCESSFUL,
  UNKNOWN_ERROR // In case the server returned e.g. a maintenance page
}

enum class UserRole {
  ADMIN, // Can add/delete users + all moderator features
  ACCESS_MANAGER, // Can give access to existing locations
  EDIT_LOCATIONS, // Can add or delete locations
  VIEW_CHECKINS, // Can access check-in data // TODO @Z change this wording in code // TODO @Z show list of rooms but no edit
}

val emailSeparators = arrayOf(" ", ",", ";")

enum class LocationAccessType {
  FREE, RESTRICTED
}