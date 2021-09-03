package com.studo.campusqr.common.payloads

import com.studo.campusqr.common.ClientPayload
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
class GuestCheckInData(
  val email: String
) : ClientPayload

@Serializable
class GuestCheckOutData(
  val email: String
) : ClientPayload

@Serializable
class LiveCheckIn(
  val activeCheckIns: Int,
  val qrCodeContent: String?
) : ClientPayload