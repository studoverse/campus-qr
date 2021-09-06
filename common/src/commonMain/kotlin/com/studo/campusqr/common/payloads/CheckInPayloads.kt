package com.studo.campusqr.common.payloads

import kotlinx.serialization.Serializable

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
class CheckInData(
  val email: String,
  val date: String? = null
) : ClientPayload

@Serializable
class CheckOutData(
  val email: String
) : ClientPayload

@Serializable
class LiveCheckIn(
  val activeCheckIns: Int,
  val qrCodeContent: String?
) : ClientPayload

@Serializable
class AllActiveCheckIns(
  val emailAddress: String
) : ClientPayload