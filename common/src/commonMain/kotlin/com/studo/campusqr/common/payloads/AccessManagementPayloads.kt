package com.studo.campusqr.common.payloads

import com.studo.campusqr.common.ClientLocation
import com.studo.campusqr.common.ClientPayload
import kotlinx.serialization.Serializable

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
class EditAccess(
  val locationId: String? = null,
  val allowedEmails: List<String>? = null,
  val dateRanges: List<ClientDateRange>? = null,
  val note: String? = null,
  val reason: String? = null,
) : ClientPayload