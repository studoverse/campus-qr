package com.studo.campusqr.common


interface ClientPayload

class ClientLocation(
  val id: String,
  val name: String,
  var checkInCount: Int,
  val accessType: String,
  val seatCount: Int?,
) : ClientPayload

val ClientLocation.accessTypeEnum get() = LocationAccessType.valueOf(accessType)

class UserData(
  var appName: String,
  var clientUser: ClientUser? = null, // Null when unauthenticated
  var externalAuthProvider: Boolean = false,
) : ClientPayload

val UserData.isAuthenticated get() = clientUser != null

class ReportData(
  val impactedUsersCount: Int,
  val impactedUsersEmails: Array<String>,
  val impactedUsersEmailsCsvData: String,
  val reportedUserLocations: Array<UserLocation>,
  val reportedUserLocationsCsv: String,
  val reportedUserLocationsCsvFileName: String,
  val startDate: String,
  val endDate: String,
  val impactedUsersEmailsCsvFileName: String,
) : ClientPayload {
  class UserLocation(
    val locationId: String,
    val locationName: String,
    val locationSeatCount: Int?,
    val email: String,
    val date: String,
    val seat: Int?,
    val potentialContacts: Int,
    val filteredSeats: Array<Int>?,
  )
}

class LocationVisitData(
  val csvData: String,
  val csvFileName: String,
) : ClientPayload

class ClientUser(
  val id: String,
  val email: String,
  val name: String,
  val rolesRaw: Array<String>,
  val firstLoginDate: String,
) : ClientPayload

val ClientUser.roles get() = rolesRaw.map { UserRole.valueOf(it) }.toSet()

// Keep in sync with BackendUser
val ClientUser.isAdmin get() = UserRole.ADMIN in roles
val ClientUser.canEditLocations get() = UserRole.EDIT_LOCATIONS in roles || isAdmin
val ClientUser.canViewCheckIns get() = UserRole.VIEW_CHECKINS in roles || isAdmin
val ClientUser.isAccessManager get() = UserRole.ACCESS_MANAGER in roles || isAdmin

class AccessManagementData(
  val accessManagement: Array<ClientAccessManagement>,
  val clientLocation: ClientLocation?,
) : ClientPayload

class AccessManagementExportData(
  val permits: Array<Permit>,
  val clientLocation: ClientLocation?,
) : ClientPayload {
  class Permit(
    val dateRange: ClientDateRange,
    val email: String,
  )
}

class ClientAccessManagement(
  val id: String,
  val locationName: String,
  val locationId: String,
  val allowedEmails: Array<String>,
  val dateRanges: Array<ClientDateRange>,
  val note: String,
  val reason: String,
) : ClientPayload

class ClientDateRange(
  val from: Double,
  val to: Double,
) : ClientPayload

class NewAccess(
  val locationId: String,
  val allowedEmails: Array<String>,
  val dateRanges: Array<ClientDateRange>,
  val note: String,
  val reason: String,
) : ClientPayload

class NewUserData(
  val email: String,
  val name: String,
  val password: String,
  val roles: Array<String>,
) : ClientPayload

class EditUserData(
  val userId: String? = null,
  val name: String?,
  val password: String?,
  val roles: Array<String>?,
) : ClientPayload

class EditAccess(
  val locationId: String? = null,
  val allowedEmails: Array<String>? = null,
  val dateRanges: Array<ClientDateRange>? = null,
  val note: String? = null,
  val reason: String? = null,
) : ClientPayload

class CreateLocation(
  val name: String,
  val accessType: LocationAccessType,
  val seatCount: Int?,
) : ClientPayload

class EditLocation(
  val name: String,
  val accessType: LocationAccessType,
  val seatCount: Int?,
) : ClientPayload

class ClientSeatFilter(
  val id: String,
  val locationId: String,
  val seat: Int,
  val filteredSeats: Array<Int>,
) : ClientPayload

class ActiveCheckIn(
  val id: String,
  val locationId: String,
  val locationName: String,
  val seat: Int?,
  val checkInDate: Double,
  val email: String,
) : ClientPayload

class EditSeatFilter(
  val seat: Int,
  val filteredSeats: List<Int>,
) : ClientPayload

class DeleteSeatFilter(
  val seat: Int,
) : ClientPayload