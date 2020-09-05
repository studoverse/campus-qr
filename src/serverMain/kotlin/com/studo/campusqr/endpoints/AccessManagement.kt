package com.studo.campusqr.endpoints

import com.studo.campusqr.common.ClientAccessManagement
import com.studo.campusqr.common.NewAccess
import com.studo.campusqr.common.UserType
import com.studo.campusqr.database.BackendAccess
import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.DateRange
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.getUser
import com.studo.katerbase.equal
import com.studo.katerbase.inArray
import io.ktor.application.*
import java.util.*

suspend fun ApplicationCall.listAccess() {
  val user = getUser()

  val params = receiveJsonMap()

  val locationId = params["locationId"]

  val accessPayloads: List<BackendAccess> = runOnDb {
    with(getCollection<BackendAccess>()) {
      when {
        locationId != null && (user.type == UserType.MODERATOR || user.type == UserType.ADMIN) -> {
          find(BackendAccess::locationId equal locationId).toList()
        }
        else -> {
          find(BackendAccess::createdBy equal user._id).toList()
        }
      }
    }
  }

  val locations = runOnDb {
    getCollection<BackendLocation>().find(
      BackendLocation::_id inArray if (locationId != null) listOf(locationId) else accessPayloads.map { it.locationId }
    ).associateBy { it._id }
  }

  respondObject(accessPayloads.map { access ->
    ClientAccessManagement(
      id = access._id,
      locationName = locations.getValue(access.locationId).name,
      allowedEmails = access.allowedEmails,
      note = access.note
    )
  })
}

suspend fun ApplicationCall.createAccess() {
  val user = getUser()

  val newAccessPayload: NewAccess = receiveClientPayload()

  val newAccess = BackendAccess().apply {
    _id = randomId()
    createdBy = user._id
    createdDate = Date()
    allowedEmails = newAccessPayload.allowedEmails
    dateRanges = newAccessPayload.dateRanges.map { DateRange(it) }
    note = newAccessPayload.note
    reason = newAccessPayload.reason
  }

  runOnDb {
    getCollection<BackendAccess>().insertOne(newAccess, upsert = false)
  }

  respondOk()
}