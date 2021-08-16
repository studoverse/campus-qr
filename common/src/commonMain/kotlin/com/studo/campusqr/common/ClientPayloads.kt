package com.studo.campusqr.common

import kotlinx.serialization.Serializable

interface ClientPayload

@Serializable
class ClientLocation(
  val id: String,
  val name: String,
  var checkInCount: Int?,
  val accessType: String,
  val seatCount: Int?,
) : ClientPayload

val ClientLocation.accessTypeEnum get() = LocationAccessType.valueOf(accessType)

@Serializable
class UserData(
  var appName: String,
  var clientUser: ClientUser? = null, // Null when unauthenticated
  var externalAuthProvider: Boolean = false,
  var liveCheckInsViewEnabled: Boolean,
) : ClientPayload

val UserData.isAuthenticated get() = clientUser != null

@Serializable
class ReportData(
  val impactedUsersCount: Int,
  val impactedUsersEmails: List<String>,
  val impactedUsersEmailsCsvData: String,
  val reportedUserLocations: List<UserLocation>,
  val reportedUserLocationsCsv: String,
  val reportedUserLocationsCsvFileName: String,
  val startDate: String,
  val endDate: String,
  val impactedUsersEmailsCsvFileName: String,
) : ClientPayload {
  @Serializable
  class UserLocation(
    val locationId: String,
    val locationName: String,
    val locationSeatCount: Int?,
    val email: String,
    val date: String,
    val seat: Int?,
    val potentialContacts: Int,
    val filteredSeats: List<Int>?,
  )
}

@Serializable
class LocationVisitData(
  val csvData: String,
  val csvFileName: String,
) : ClientPayload

@Serializable
class ClientUser(
  val id: String,
  val email: String,
  val name: String,
  val permissionsRaw: List<String>,
  val firstLoginDate: String,
) : ClientPayload

val ClientUser.permissions: Set<UserPermission> get() = permissionsRaw.map { UserPermission.valueOf(it) }.toSet()

// Keep in sync with BackendUser
val ClientUser.canEditUsers get() = UserPermission.EDIT_USERS in permissions
val ClientUser.canEditLocations get() = UserPermission.EDIT_LOCATIONS in permissions
val ClientUser.canViewCheckIns get() = UserPermission.VIEW_CHECKINS in permissions
val ClientUser.canEditAnyLocationAccess get() = canEditOwnLocationAccess || canEditAllLocationAccess
val ClientUser.canEditOwnLocationAccess get() = UserPermission.EDIT_OWN_ACCESS in permissions
val ClientUser.canEditAllLocationAccess get() = UserPermission.EDIT_ALL_ACCESS in permissions

@Serializable
class AccessManagementData(
  val accessManagement: List<ClientAccessManagement>,
  val clientLocation: ClientLocation?,
) : ClientPayload

@Serializable
class AccessManagementExportData(
  val permits: List<Permit>,
  val clientLocation: ClientLocation?,
) : ClientPayload {
  @Serializable
  class Permit(
    val dateRange: ClientDateRange,
    val email: String,
  )
}

@Serializable
class ClientAccessManagement(
  val id: String,
  val locationName: String,
  val locationId: String,
  val allowedEmails: List<String>,
  val dateRanges: List<ClientDateRange>,
  val note: String,
  val reason: String,
) : ClientPayload

@Serializable
class ClientDateRange(
  val from: Double,
  val to: Double,
) : ClientPayload

@Serializable
class NewAccess(
  val locationId: String,
  val allowedEmails: List<String>,
  val dateRanges: List<ClientDateRange>,
  val note: String,
  val reason: String,
) : ClientPayload

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
class EditAccess(
  val locationId: String? = null,
  val allowedEmails: List<String>? = null,
  val dateRanges: List<ClientDateRange>? = null,
  val note: String? = null,
  val reason: String? = null,
) : ClientPayload

@Serializable
class CreateLocation(
  val name: String,
  val accessType: LocationAccessType,
  val seatCount: Int?,
) : ClientPayload

@Serializable
class EditLocation(
  val name: String,
  val accessType: LocationAccessType,
  val seatCount: Int?,
) : ClientPayload

@Serializable
class ClientSeatFilter(
  val id: String,
  val locationId: String,
  val seat: Int,
  val filteredSeats: List<Int>,
) : ClientPayload

@Serializable
class ActiveCheckIn(
  val id: String,
  val locationId: String,
  val locationName: String,
  val seat: Int?,
  val checkInDate: Double,
  val email: String,
) : ClientPayload

@Serializable
class EditSeatFilter(
  val seat: Int,
  val filteredSeats: List<Int>,
) : ClientPayload

@Serializable
class DeleteSeatFilter(
  val seat: Int,
) : ClientPayload

@Serializable
class LiveCheckIn(
  val activeCheckIns: Int,
  val qrCodeContent: String?
) : ClientPayload