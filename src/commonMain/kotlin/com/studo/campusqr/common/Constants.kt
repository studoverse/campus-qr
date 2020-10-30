package com.studo.campusqr.common

enum class LoginResult {
  LOGIN_FAILED,
  LOGIN_SUCCESSFUL,
  UNKNOWN_ERROR // In case the server returned e.g. a maintenance page
}

enum class UserRole {
  ADMIN, // Can add/delete users + all moderator features
  ACCESS_MANAGER, // Can give access to existing locations
  LOCATION_MANAGER, // Can add or delete locations
  INFECTION_MANAGER, // Can access check-in data
}

val emailSeparators = arrayOf(" ", ",", ";")

enum class LocationAccessType {
  FREE, RESTRICTED
}