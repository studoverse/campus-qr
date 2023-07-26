package util

import app.baseUrl
import com.studo.campusqr.common.utils.LocalizedString

val pathBase = baseUrl.substringAfter("://").dropWhile { it != '/' }.substringBefore("/admin") + "/admin"
val apiBase = baseUrl

enum class Url(
  override val path: String,
  override val title: LocalizedString,
  override val requiresAuth: Boolean = true,
  override val showWithShell: Boolean = true,
) : MbUrl {
  // Kotlin urls
  BLANK(pathBase, LocalizedString("", "")),
  LOGIN_EMAIL("$pathBase/login", LocalizedString("Log-in", "Einloggen"), requiresAuth = false, showWithShell = false),
  ACCESS_MANAGEMENT_LIST("$pathBase/access", Strings.access_control, requiresAuth = true),
  ACCESS_MANAGEMENT_LIST_EXPORT("$pathBase/access/export", Strings.access_control, requiresAuth = true),
  ACCESS_MANAGEMENT_LOCATION_LIST("$pathBase/locationAccess/:id", Strings.access_control, requiresAuth = true),
  ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT(
    "$pathBase/locationAccess/:id/export",
    Strings.access_control,
    requiresAuth = true
  ),

  GUEST_CHECK_IN("$pathBase/guest-check-in", Strings.guest_checkin, requiresAuth = true),
  LOCATIONS_LIST("$pathBase/locations", Strings.locations, requiresAuth = true),
  REPORT("$pathBase/report", Strings.report, requiresAuth = true),
  USERS("$pathBase/users", Strings.user_management, requiresAuth = true),
  ACCOUNT_SETTINGS("$pathBase/accountSettings", Strings.account_settings, requiresAuth = true),
  ADMIN_INFO("$pathBase/adminInfo", Strings.admin_info, requiresAuth = true),
}