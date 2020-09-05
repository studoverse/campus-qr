package com.studo.campusqr.common


interface ClientPayload

class ClientLocation(
  val id: String,
  val name: String,
  val checkInCount: Int
) : ClientPayload

class UserData : ClientPayload {
  lateinit var appName: String
  var clientUser: ClientUser? = null // Null when unauthenticated
}

val UserData.isAuthenticated get() = clientUser != null

class ReportData(
  val impactedUsersCount: Int,
  val impactedUsersMailtoLink: String,
  val impactedUsersEmailsCsvData: String,
  val reportedUserLocations: Array<UserLocation>,
  val reportedUserLocationsCsv: String,
  val reportedUserLocationsCsvFileName: String,
  val startDate: String,
  val endDate: String,
  val impactedUsersEmailsCsvFileName: String
) : ClientPayload {
  class UserLocation(val email: String, val date: String, val locationName: String)
}

class LocationVisitData(
  val csvData: String,
  val csvFileName: String
) : ClientPayload

class ClientUser(
  val id: String,
  val email: String,
  val name: String,
  val type: String,
  val firstLoginDate: String
) : ClientPayload

class ClientAccessManagement(
  val id: String,
  val locationName: String,
  val allowedEmails: List<String>,
  val note: String
) : ClientPayload

class ClientDateRange(
  val from: Long,
  val to: Long
) : ClientPayload

class NewAccess(
  val allowedEmails: List<String>,
  val dateRanges: List<ClientDateRange>,
  val note: String,
  val reason: String
)