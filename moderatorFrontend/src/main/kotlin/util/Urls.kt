package util

import app.baseUrl
import com.studo.campusqr.common.utils.LocalizedString

val pathBase = baseUrl.substringAfter("://").dropWhile { it != '/' }.substringBefore("/admin") + "/admin"
val apiBase = baseUrl

sealed class Url(
  path: String,
  title: LocalizedString,
  requiresAuth: Boolean = true,
  showWithShell: Boolean = true,
) : MbUrl(path, title, requiresAuth, showWithShell) {
  // Kotlin urls
  object BLANK : Url(pathBase, LocalizedString("", ""))
  object LOGIN_EMAIL : Url("$pathBase/login", LocalizedString("Log-in", "Einloggen"), requiresAuth = false, showWithShell = false)
  object ACCESS_MANAGEMENT_LIST : Url("$pathBase/access", Strings.access_control, requiresAuth = true)
  object ACCESS_MANAGEMENT_LIST_EXPORT : Url("$pathBase/access/export", Strings.access_control, requiresAuth = true)
  object ACCESS_MANAGEMENT_LOCATION_LIST : Url("$pathBase/locationAccess/:id", Strings.access_control, requiresAuth = true)
  object ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT : Url(
    "$pathBase/locationAccess/:id/export",
    Strings.access_control,
    requiresAuth = true
  )

  object GUEST_CHECK_IN : Url("$pathBase/guest-check-in", Strings.guest_checkin, requiresAuth = true)
  object LOCATIONS_LIST : Url("$pathBase/locations", Strings.locations, requiresAuth = true)
  object REPORT : Url("$pathBase/report", Strings.report, requiresAuth = true)
  object USERS : Url("$pathBase/users", Strings.user_management, requiresAuth = true)
  object ACCOUNT_SETTINGS : Url("$pathBase/accountSettings", Strings.account_settings, requiresAuth = true)
  object ADMIN_INFO : Url("$pathBase/adminInfo", Strings.admin_info, requiresAuth = true)
}