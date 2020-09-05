package com.studo.campusqr.endpoints

import com.studo.campusqr.common.ClientAccessManagement
import com.studo.campusqr.common.EditAccess
import com.studo.campusqr.common.NewAccess
import com.studo.campusqr.common.UserType
import com.studo.campusqr.database.BackendAccess
import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.DateRange
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.katerbase.equal
import com.studo.katerbase.inArray
import java.util.*

suspend fun getAccess(id: String): BackendAccess? = runOnDb {
  getCollection<BackendAccess>().findOne(BackendAccess::_id equal id)
}

suspend fun AuthenticatedApplicationCall.listAccess() {
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

suspend fun AuthenticatedApplicationCall.createAccess() {
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

suspend fun AuthenticatedApplicationCall.deleteAccess() {
  val accessId = parameters["id"]!!

  val access = getAccess(accessId) ?: throw IllegalArgumentException("Access doesn't exist")

  if (user._id != access.createdBy && !user.isModerator) {
    respondForbidden()
    return
  }

  runOnDb {
    getCollection<BackendAccess>().deleteOne(BackendAccess::_id equal access._id)
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.duplicateAccess() {
  val accessId = parameters["id"]!!

  val oldAccess = getAccess(accessId) ?: throw IllegalArgumentException("Access doesn't exist")
  val newAccess = oldAccess.apply {
    _id = randomId()
  }

  runOnDb {
    getCollection<BackendAccess>().insertOne(newAccess, upsert = false)
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.editAccess() {
  val accessId = parameters["id"]!!
  val access = getAccess(accessId) ?: throw IllegalArgumentException("Access doesn't exist")

  if (user._id != access.createdBy && !user.isModerator) {
    respondForbidden()
    return
  }

  val editAccessPayload: EditAccess = receiveClientPayload()

  runOnDb {
    getCollection<BackendAccess>().updateOne(BackendAccess::_id equal accessId) {
      with(editAccessPayload) {
        if (locationId != null) {
          BackendAccess::locationId setTo locationId
        }
        if (allowedEmails != null) {
          BackendAccess::allowedEmails setTo allowedEmails
        }
        if (dateRanges != null) {
          BackendAccess::dateRanges setTo dateRanges.map { DateRange(it) }
        }
        if (note != null) {
          BackendAccess::note setTo note
        }
        if (reason != null) {
          BackendAccess::reason setTo reason
        }
      }
    }
  }

  respondOk()
}