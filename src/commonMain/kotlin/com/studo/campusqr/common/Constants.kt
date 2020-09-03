package com.studo.campusqr.common

enum class LoginResult {
  LOGIN_FAILED,
  LOGIN_SUCCESSFUL,
  UNKNOWN_ERROR // In case the server returned e.g. a maintenance page
}

enum class UserType {
  ADMIN, // Can add/delete users + all moderator features
  MODERATOR // Can create new rooms, and manage check-in data
}

val reportEmailSeparators = arrayOf(" ", ",", ";")