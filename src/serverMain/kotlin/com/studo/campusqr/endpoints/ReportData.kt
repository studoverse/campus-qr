package com.studo.campusqr.endpoints

import com.studo.campusqr.common.ActiveCheckIn
import com.studo.campusqr.common.ReportData
import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.extensions.*
import com.studo.campusqr.serverScope
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.katerbase.*
import kotlinx.coroutines.Deferred
import com.studo.katerbase.equal
import com.studo.katerbase.greaterEquals
import com.studo.katerbase.inArray
import com.studo.katerbase.inRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*

/**
 * This file contains the contact tracing endpoint.
 */
suspend fun AuthenticatedApplicationCall.returnReportData() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val params = receiveJsonStringMap()

  val now = Date()
  val emails = params.getValue("email").split(*emailSeparators).filter { it.isNotEmpty() }
  val oldestDate = params["oldestDate"]?.toLong()?.let { Date(it) } ?: now.addDays(-14)

  val reportedUserCheckIns: List<CheckIn> = runOnDb {
    getCollection<CheckIn>()
      .find(CheckIn::email inArray emails, CheckIn::date greaterEquals oldestDate)
      .sortByDescending(CheckIn::date)
      .toList()
  }

  val locationMapTask: Deferred<Map<String, BackendLocation>> = serverScope.async(Dispatchers.IO) {
    runOnDb {
      getCollection<BackendLocation>()
        .find(BackendLocation::_id inArray reportedUserCheckIns.map { it.locationId }.distinct())
        .associateBy { it._id }
    }
  }

  val impactedCheckInsTask: Deferred<List<CheckIn>> = serverScope.async(Dispatchers.IO) {
    runOnDb {
      // Keep logic in sync with listAllActiveCheckIns()
      val previousInfectionHours: Int = getConfig("previousInfectionHours")
      val nextInfectionHours: Int = getConfig("nextInfectionHours")

      // We need to probably optimize performance here in the future
      val otherCheckIns = reportedUserCheckIns.flatMap { checkIn ->
        getCollection<CheckIn>().find(
          CheckIn::locationId equal checkIn.locationId,
          CheckIn::date.inRange(
            checkIn.date.addHours(-previousInfectionHours),
            checkIn.date.addHours(nextInfectionHours)
          )
        ).toList()
      }

      otherCheckIns.distinctBy { it.email }.filter { it.email !in emails }
    }
  }

  val seatFilterTask: Deferred<Map<String, BackendSeatFilter>> = serverScope.async(Dispatchers.IO) {
    runOnDb {
      reportedUserCheckIns.mapNotNull { checkIn ->
        checkIn.seat?.let { seat ->
          getCollection<BackendSeatFilter>()
            .findOne(
              BackendSeatFilter::locationId equal checkIn.locationId,
              BackendSeatFilter::seat equal seat
            )
        }
      }.associateBy { it.locationId }
    }
  }

  val locationIdToLocationMap = locationMapTask.await()
  var impactedCheckIns = impactedCheckInsTask.await()
  val locationIdToSeatFilterMap = seatFilterTask.await()

  if (locationIdToSeatFilterMap.isNotEmpty()) {
    impactedCheckIns = impactedCheckIns.filter {
      locationIdToSeatFilterMap[it.locationId]?.let { seatFilter ->
        it.seat in seatFilter.filteredSeats
      } ?: true // checkIn is impacted by default, if filter doesn't exist
    }
  }

  val impactedUsersEmails = impactedCheckIns.map { it.email }

  val csvFilePrefix = emails
    .firstOrNull()
    ?.map { if (it.isLetterOrDigit()) it else '-' }
    ?.joinToString(separator = "")
    ?.take(20)

  respondObject(
    ReportData(
      impactedUsersCount = impactedUsersEmails.count(),
      impactedUsersMailtoLink = "mailto:?bcc=" + impactedUsersEmails.joinToString(","),
      reportedUserLocations = reportedUserCheckIns.map { checkIn ->
        val location = locationIdToLocationMap.getValue(checkIn.locationId)
        ReportData.UserLocation(
          locationId = location._id,
          locationName = location.name,
          locationSeatCount = location.seatCount,
          email = checkIn.email,
          date = checkIn.date.toAustrianTime(yearAtBeginning = false),
          seat = checkIn.seat,
          filteredSeats = locationIdToSeatFilterMap[location._id]?.filteredSeats?.toTypedArray()
        )
      }.toTypedArray(),
      impactedUsersEmailsCsvData = impactedUsersEmails.joinToString("\n"),
      impactedUsersEmailsCsvFileName = "${csvFilePrefix?.plus("-emails") ?: "emails"}.csv",
      reportedUserLocationsCsv = "sep=;\n" + reportedUserCheckIns.joinToString("\n") {
        "${it.email};${it.date.toAustrianTime(yearAtBeginning = false)};${locationIdToLocationMap.getValue(it.locationId).name}"
      },
      reportedUserLocationsCsvFileName = "${csvFilePrefix?.plus("-checkins") ?: "checkins"}.csv",
      startDate = oldestDate.toAustrianTime("dd.MM.yyyy"),
      endDate = now.toAustrianTime("dd.MM.yyyy"),
    )
  )
}

/**
 * This endpoint returns all active check-ins of one single user.
 * Might be useful for direct API access.
 */
suspend fun AuthenticatedApplicationCall.listAllActiveCheckIns() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val params = receiveJsonStringMap()
  val emailAddress = params.getValue("emailAddress")

  val checkIns = runOnDb {
    // Keep logic in sync with returnReportData()
    val nextInfectionHours: Int = getConfig("nextInfectionHours")
    getCollection<CheckIn>()
        .find(CheckIn::email equal emailAddress, CheckIn::date greater Date().addHours(-nextInfectionHours))
        .sortByDescending(CheckIn::date)
        .toList()
  }

  val locationMap = runOnDb {
    getCollection<BackendLocation>()
        .find(BackendLocation::_id inArray checkIns.map { it.locationId }.distinct())
        .associateBy(keySelector = { it._id }, valueTransform = { it.name })
  }

  respondObject(
      checkIns.map { checkIn ->
        ActiveCheckIn(
            id = checkIn._id,
            locationId = checkIn.locationId,
            locationName = locationMap.getValue(checkIn.locationId),
            seat = checkIn.seat,
            checkInDate = checkIn.date.time.toDouble()
        )
      }
  )
}