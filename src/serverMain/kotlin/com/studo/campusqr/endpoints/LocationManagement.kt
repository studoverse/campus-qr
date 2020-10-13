package com.studo.campusqr.endpoints

import com.studo.campusqr.common.*
import com.studo.campusqr.common.extensions.emailRegex
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.database.*
import com.studo.campusqr.database.MainDatabase.getConfig
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.campusqr.utils.getAuthenticatedCall
import com.studo.katerbase.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import java.util.*

/**
 * This file contains every endpoint which is used in the location management.
 */
suspend fun getLocation(id: String): BackendLocation {
  return getLocationOrNull(id) ?: throw BadRequestException("No location for id")
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

  val params: CreateLocation = receiveClientPayload()

  val room = BackendLocation().apply {
    this._id = randomId().take(20) // 20 Characters per code to make it better detectable
    this.name = params.name.trim()
    this.createdDate = Date()
    this.createdBy = user._id
    this.checkInCount = 0
    this.accessType = params.accessType
    this.seatCount = params.seatCount?.coerceIn(1, 10_000)
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

private data class UserLocation(val locationId: String, val seat: Int?)

private fun ApplicationCall.getUserLocation(): UserLocation {
  // id-parameter is either "$locationId" or "$locationId-$seat" (depending if location has seatCount defined or not)
  val fullLocationId = parameters["id"] ?: throw BadRequestException("No locationId was provided")
  return UserLocation(
      locationId = fullLocationId.substringBefore("-"),
      seat = fullLocationId.substringAfter("-", missingDelimiterValue = "").emptyToNull()?.toIntOrNull()
  )
}

private fun validateSeatForLocation(location: BackendLocation, seat: Int?) {
  if (location.seatCount != null) {
    when {
      seat == null -> throw BadRequestException("No seat provided but location has seats defined")
      seat <= 0 -> throw BadRequestException("Seat must be > 0")
      seat > location.seatCount!! -> throw BadRequestException("Seat must be <= location.seatCount")
    }
  } else if (seat != null) {
    throw BadRequestException("Seat provided but location has no seats defined")
  }
}

suspend fun ApplicationCall.visitLocation(checkedInBy: String? = null) {
  val params = receiveJsonStringMap()

  val (locationId, seat) = getUserLocation()

  val location = getLocation(locationId)

  // Validate seat argument
  validateSeatForLocation(location, seat)

  val email = params["email"]?.trim()?.toLowerCase() ?: throw BadRequestException("No email was provided")
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
    respondError("forbidden_email") // At least do a very basic email validation to catch common errors
    return
  }
  if (runOnDb { getConfig("emailAccessRegex") as String }.emptyToNull()?.let { email.matches(Regex(it)) } == false) {
    respondError("forbidden_email") // E-Mail did not match specified regex (e.g. not a university email address)
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
      respondError("forbidden_access_restricted")
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
    this.checkedInBy = checkedInBy
  }

  runOnDb {
    getCollection<CheckIn>().insertOne(checkIn, upsert = false)

    getCollection<BackendLocation>().updateOne(BackendLocation::_id equal location._id) {
      BackendLocation::checkInCount incrementBy 1
    }
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.guestVisitLocation() {
  if (user.type == UserType.ACCESS_MANAGER || user.isAdmin) {
    visitLocation(checkedInBy = user._id)
  } else {
    respondForbidden()
  }
}

suspend fun ApplicationCall.checkOutLocation() {
  val params = receiveJsonStringMap()
  val (locationId, seat) = getUserLocation()

  val location = getLocation(locationId)

  // Validate seat argument
  validateSeatForLocation(location, seat)

  val email = params["email"]?.trim()?.toLowerCase() ?: throw BadRequestException("No email was provided")

  runOnDb {
    // Use updateMany here, user could check-in multiple times
    getCollection<CheckIn>().updateMany(
      CheckIn::email equal email,
      CheckIn::locationId equal locationId,
      CheckIn::checkOutDate equal null
    ) {
      CheckIn::checkOutDate setTo Date()
    }
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.returnLocationVisitCsvData() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"] ?: throw BadRequestException("No locationId provided")

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

  val locationId = parameters["id"] ?: throw BadRequestException("No locationId provided")
  val location = getLocation(locationId)

  val params: EditLocation = receiveClientPayload()

  runOnDb {
    getCollection<BackendLocation>().updateOne(BackendLocation::_id equal locationId) {
      BackendLocation::name setTo params.name
      BackendLocation::accessType setTo params.accessType
      BackendLocation::seatCount setTo params.seatCount?.coerceIn(1, 10_000)
    }

    // In case we change the seatCount, delete previous seatCounts of this location as the seating-plan might have changed
    if (location.seatCount != params.seatCount) {
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

  val locationId = parameters["id"] ?: throw BadRequestException("No locationId provided")

  runOnDb {
    getCollection<BackendLocation>().deleteOne(BackendLocation::_id equal locationId)
    getCollection<CheckIn>().deleteMany(CheckIn::locationId equal locationId)
    getCollection<BackendAccess>().deleteMany(BackendAccess::locationId equal locationId)
    getCollection<BackendSeatFilter>().deleteMany(BackendSeatFilter::locationId equal locationId)
  }

  respondOk()
}
