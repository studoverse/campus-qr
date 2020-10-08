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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*

//@formatter:off
/**
Contact tracing:

checkIn               checkOut
+       infected      +
+---------------------+
+                     +

+     a     +  +    b
+-----------+  +---------->
+           +  +

+    c    +           +    d    +
+---------+           +---------+
+         +           +         +

+   e   +                               +    f    +
+-------+                               +---------+
+       +                               +         +

+               g               +
+-------------------------------+
+                               +

We treat the following cases as k1 contact person:
- a, c, d and g because their time range intersect the infected persons time range
- b because his time range intersects the infected persons time range, although not having a [CheckIn.checkOutDate]
We do NOT treat the following cases as k1 contact person:
- e and f because their time range doesn't intersect the infected persons time range

Note that infected checkIn and checkOut timestamp get extened by [transitThresholdSeconds]
 */
//@formatter:on
internal suspend fun generateContactTracingReport(emails: List<String>, oldestDate: Date): ReportData {
  val now = Date()

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

  data class Contact(val reportedCheckIn: CheckIn, val impactedCheckIns: List<CheckIn>)

  val contactsTask: Deferred<List<Contact>> = serverScope.async(Dispatchers.IO) {
    runOnDb {
      val transitThresholdSeconds: Int = getConfig("transitThresholdSeconds")

      // We need to probably optimize performance here in the future
      val impactedCheckIns = reportedUserCheckIns.map { reportedUserCheckin ->
        val impactedCheckIns = getCollection<CheckIn>()
          .find(
            CheckIn::locationId equal reportedUserCheckin.locationId,
            CheckIn::date lowerEquals (reportedUserCheckin.checkOutDate ?: now).addSeconds(transitThresholdSeconds),
            or(
              CheckIn::checkOutDate greaterEquals reportedUserCheckin.date.addSeconds(-transitThresholdSeconds),
              CheckIn::checkOutDate equal null // Other user has not checked out yet
            )
          )
          .distinctBy { it.email }
          .filter { it.email !in emails }

        return@map Contact(reportedUserCheckin, impactedCheckIns)
      }

      impactedCheckIns
    }
  }

  fun BackendSeatFilter.mapId() = "$locationId-$seat"
  fun CheckIn.mapId() = "$locationId-$seat"

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
      }.associateBy { it.mapId() }
    }
  }

  val locationIdToLocationMap: Map<String, BackendLocation> = locationMapTask.await()
  var contacts: List<Contact> = contactsTask.await()
  val seatFilterMap: Map<String, BackendSeatFilter> = seatFilterTask.await()

  // Seat filters
  if (seatFilterMap.isNotEmpty()) {
    contacts = contacts.map { contact ->
      val seatFilter = seatFilterMap[contact.reportedCheckIn.mapId()]
      if (seatFilter == null) {
        contact // No change
      } else {
        // Apply seat filter
        Contact(contact.reportedCheckIn, contact.impactedCheckIns.filter { impactedCheckIn ->
          impactedCheckIn.seat in seatFilter.filteredSeats
        })
      }
    }
  }

  val impactedUsers = contacts
    .flatMap { it.impactedCheckIns }
    .distinctBy { it.email }

  val impactedUsersEmails = impactedUsers.map { it.email }

  val csvFilePrefix = emails
    .firstOrNull()
    ?.map { if (it.isLetterOrDigit()) it else '-' }
    ?.joinToString(separator = "")
    ?.take(20)

  return ReportData(
    impactedUsersEmails = impactedUsersEmails.toTypedArray(),
    impactedUsersCount = impactedUsers.count(),
    impactedUsersMailtoLink = "mailto:?bcc=" + impactedUsersEmails.joinToString(","), // TODO move to frontend
    reportedUserLocations = contacts.map { (reportedCheckIn, impactedCheckIns) ->
      val location = locationIdToLocationMap.getValue(reportedCheckIn.locationId)
      ReportData.UserLocation(
        locationId = location._id,
        locationName = location.name,
        locationSeatCount = location.seatCount,
        email = reportedCheckIn.email,
        date = reportedCheckIn.date.toAustrianTime(yearAtBeginning = false),
        seat = reportedCheckIn.seat,
        filteredSeats = seatFilterMap[reportedCheckIn.mapId()]?.filteredSeats?.toTypedArray(),
        impactedPeople = impactedCheckIns.count()
      )
    }.toTypedArray(),
    impactedUsersEmailsCsvData = impactedUsersEmails.joinToString("\n"),
    impactedUsersEmailsCsvFileName = "${csvFilePrefix?.plus("-emails") ?: "emails"}.csv",
    reportedUserLocationsCsv = "sep=;\n" + reportedUserCheckIns.joinToString("\n") {
      "${it.email};${it.date.toAustrianTime(yearAtBeginning = false)};${locationIdToLocationMap.getValue(it.locationId).name};${it.seat ?: "-"}"
    },
    reportedUserLocationsCsvFileName = "${csvFilePrefix?.plus("-checkins") ?: "checkins"}.csv",
    startDate = oldestDate.toAustrianTime("dd.MM.yyyy"),
    endDate = now.toAustrianTime("dd.MM.yyyy"),
  )
}

suspend fun AuthenticatedApplicationCall.returnReportData() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val params = receiveJsonStringMap()

  val now = Date()
  val emails = params.getValue("email").split(*emailSeparators).filter { it.isNotEmpty() }
  val oldestDate = params["oldestDate"]?.toLong()?.let { Date(it) } ?: now.addDays(-14)

  respondObject(generateContactTracingReport(emails, oldestDate))
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
    getCollection<CheckIn>()
      .find(CheckIn::email equal emailAddress, CheckIn::checkOutDate equal null)
      .sortByDescending(CheckIn::date) // No need for an index here, this is probably a very small list
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