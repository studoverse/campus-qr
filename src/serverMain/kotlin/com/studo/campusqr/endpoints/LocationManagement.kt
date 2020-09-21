package com.studo.campusqr.endpoints

import com.studo.campusqr.common.ClientLocation
import com.studo.campusqr.common.LocationAccessType
import com.studo.campusqr.common.LocationVisitData
import com.studo.campusqr.common.extensions.emailRegex
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.database.*
import com.studo.campusqr.database.MainDatabase.getConfig
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.katerbase.*
import io.ktor.application.*
import io.ktor.http.*
import java.util.*

/**
 * This file contains every endpoint which is used in the location management.
 */
suspend fun getLocation(id: String): BackendLocation {
  return getLocationOrNull(id) ?: throw IllegalArgumentException("No location for id")
}

suspend fun getLocationOrNull(id: String): BackendLocation? {
  return runOnDb { getCollection<BackendLocation>().findOne(BackendLocation::_id equal id) }
}

suspend fun getAllLocations(language: String): List<ClientLocation> {
  return runOnDb { getCollection<BackendLocation>().find().toList() }.map { it.toClientClass(language) }
}

suspend fun AuthenticatedApplicationCall.createLocation() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val params = receiveJsonMap()

  val name = params["name"]?.trim() ?: throw IllegalArgumentException("No name was provided")
  val accessType = params["accessType"]?.let { LocationAccessType.valueOf(it) }
    ?: throw IllegalArgumentException("No accessType was provided")
  val seatCount = params["seatCount"]?.toIntOrNull()?.coerceIn(1, 10_000)

  val room = BackendLocation().apply {
    this._id = randomId().take(20) // 20 Characters per code to make it better detectable
    this.name = name
    this.createdDate = Date()
    this.createdBy = user._id
    this.checkInCount = 0
    this.accessType = accessType
    this.seatCount = seatCount
  }

  MainDatabase.getCollection<BackendLocation>().insertOne(room, upsert = false)

  respondOk()
}

suspend fun AuthenticatedApplicationCall.listLocations() {
  if (!sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  val locations = getAllLocations(language)
  respondObject(locations)
}

suspend fun ApplicationCall.visitLocation() {
  val params = receiveJsonStringMap()

  // id-parameter is either "$locationId" or "$locationId-$seat" (depending if location has seatCount defined or not)
  val fullLocationId = parameters["id"] ?: throw IllegalArgumentException("No locationId was provided")
  val locationId = fullLocationId.substringBefore("-")
  val seat = fullLocationId.substringAfter("-", missingDelimiterValue = "").emptyToNull()?.toIntOrNull()
  val location = getLocation(locationId)

  // Validate seat argument
  if (location.seatCount != null) {
    when {
      seat == null -> throw IllegalArgumentException("No seat provided but location has seats defined")
      seat <= 0 -> throw IllegalArgumentException("Seat must be > 0")
      seat > location.seatCount!! -> throw IllegalArgumentException("Seat must be <= location.seatCount")
    }
  } else if (seat != null) {
    throw IllegalArgumentException("Seat provided but location has no seats defined")
  }


  val email = params["email"]?.trim() ?: throw IllegalArgumentException("No email was provided")
  val now = Date()

  // Clients can send a custom visit date
  // This is useful for offline dispatching, we want to save the date of the visit and not when the
  // request arrives on the server.
  val visitDate = params["date"]?.let { Date(it.toLong()) }

  // Don't allow dates older than 7 days
  if (visitDate != null && visitDate > now.addDays(days = -7)) {
    respondOk()
    return
  }

  if (!email.matches(emailRegex) || email.count() > 100) {
    respondError("email_not_valid") // At least do a very basic email validation to catch common errors
    return
  }
  var accessId: String? = null

  if (location.accessType == LocationAccessType.RESTRICTED) {
    val access = runOnDb {
      getCollection<BackendAccess>().findOne(
        BackendAccess::locationId equal location._id,
        BackendAccess::allowedEmails has email,
        BackendAccess::dateRanges.any(
          DateRange::from lowerEquals now,
          DateRange::to greaterEquals now
        )
      )
    }

    if (access == null) {
      respondForbidden()
      return
    } else {
      accessId = access._id
    }
  }

  val checkIn = CheckIn().apply {
    this._id = randomId()
    this.locationId = location._id
    this.date = visitDate ?: now
    this.email = email
    this.userAgent = if (getConfig("storeCheckInUserAgent")) request.headers[HttpHeaders.UserAgent] ?: "" else null
    this.ipAddress = getConfig<String>("checkInIpAddressHeader").emptyToNull()?.let { request.headers[it] }
    this.grantAccessId = accessId
    this.seat = seat
  }

  runOnDb {
    getCollection<CheckIn>().insertOne(checkIn, upsert = false)

    getCollection<BackendLocation>().updateOne(BackendLocation::_id equal location._id) {
      BackendLocation::checkInCount incrementBy 1
    }
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.returnLocationVisitCsvData() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"]!!

  val location = runOnDb { getLocation(locationId) }

  val checkIns = runOnDb {
    getCollection<CheckIn>()
        .find(CheckIn::locationId equal locationId)
        .sortByDescending(CheckIn::date)
        .toList()
  }

  respondObject(
    LocationVisitData(
      csvData = "sep=;\n" + checkIns.joinToString(separator = "\n") { "${it.email};${it.date.toAustrianTime()}" },
      csvFileName = "${location.name}.csv"
    )
  )
}

suspend fun AuthenticatedApplicationCall.editLocation() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"]!!
  val location = getLocation(locationId)

  val params = receiveJsonMap()

  val name = (params["name"] as? String)?.trim() ?: throw IllegalArgumentException("No name was provided")
  val accessType = (params["accessType"] as? String)?.let { LocationAccessType.valueOf(it) }
      ?: throw IllegalArgumentException("No accessType was provided")
  val seatCount = (params["seatCount"] as? Int)?.coerceIn(1, 10_000)

  runOnDb {
    getCollection<BackendLocation>().updateOne(BackendLocation::_id equal locationId) {
      BackendLocation::name setTo name
      BackendLocation::accessType setTo accessType
      BackendLocation::seatCount setTo seatCount
    }

    // In case we change the seatCount, delete previous seatCounts of this location as the seating-plan might have changed
    if (location.seatCount != seatCount) {
      getCollection<BackendSeatFilter>().deleteMany(BackendAccess::locationId equal locationId)
    }
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.deleteLocation() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"]!!

  runOnDb {
    getCollection<BackendLocation>().deleteOne(BackendLocation::_id equal locationId)
    getCollection<CheckIn>().deleteMany(CheckIn::locationId equal locationId)
    getCollection<BackendAccess>().deleteMany(BackendAccess::locationId equal locationId)
    getCollection<BackendSeatFilter>().deleteMany(BackendSeatFilter::locationId equal locationId)
  }

  respondOk()
}
