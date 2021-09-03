package com.studo.campusqr.common.payloads

import com.studo.campusqr.common.ClientPayload
import com.studo.campusqr.common.LocationAccessType
import kotlinx.serialization.Serializable

@Serializable
class LocationVisitData(
  val csvData: String,
  val csvFileName: String,
) : ClientPayload

@Serializable
class CreateOrUpdateLocationData(
  val name: String,
  val accessType: LocationAccessType,
  val seatCount: Int?
) : ClientPayload