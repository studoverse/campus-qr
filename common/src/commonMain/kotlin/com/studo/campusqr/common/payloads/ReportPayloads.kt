package com.studo.campusqr.common.payloads

import kotlinx.serialization.Serializable

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
class EditSeatFilter(
  val seat: Int,
  val filteredSeats: List<Int>,
) : ClientPayload

@Serializable
class DeleteSeatFilter(
  val seat: Int,
) : ClientPayload

@Serializable
class TraceContactsReportData(
  val email: String,
  val oldestDate: String
) : ClientPayload

@Serializable
class ClientSeatFilter(
  val id: String,
  val locationId: String,
  val seat: Int,
  val filteredSeats: List<Int>,
) : ClientPayload