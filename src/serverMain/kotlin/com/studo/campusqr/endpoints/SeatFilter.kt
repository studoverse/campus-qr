package com.studo.campusqr.endpoints

import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.extensions.receiveJsonMap
import com.studo.campusqr.extensions.respondForbidden
import com.studo.campusqr.extensions.respondOk
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.katerbase.MongoMainEntry.Companion.generateId
import com.studo.katerbase.equal
import java.util.*

/**
 * This file contains endpoints which are used in to modify location seat filters.
 */

// Edit or create seat filter
suspend fun AuthenticatedApplicationCall.editSeatFilter() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"]!!
  val location = getLocation(locationId)

  val params = receiveJsonMap()
  val seat = params.getValue("seat") as Int
  val filteredSeats = params.getValue("filteredSeats") as List<*>

  if (filteredSeats.any { it !is Int }) throw IllegalArgumentException("filteredSeats must be [Int]")
  @Suppress("UNCHECKED_CAST") // Checked in the line above
  filteredSeats as List<Int>

  // Validate seat & filteredSeats argument
  if (location.seatCount == null) {
    throw IllegalArgumentException("SeatFilter can not be inserted on location with no seats")
  } else if (seat <= 0) {
    throw IllegalArgumentException("Seat must be > 0")
  } else if (seat > location.seatCount!!) {
    throw IllegalArgumentException("Seat must be <= location.seatCount")
  } else if (filteredSeats.any { it <= 0 }) {
    throw IllegalArgumentException("FilteredSeats must all be > 0")
  } else if (filteredSeats.any { it > location.seatCount!! }) {
    throw IllegalArgumentException("FilteredSeats must all be <= location.seatCount")
  }

  runOnDb {
    getCollection<BackendSeatFilter>().insertOne(BackendSeatFilter().also { filter ->
      filter._id = generateId(locationId, seat.toString())
      filter.locationId = locationId
      filter.seat = seat
      filter.editedBy = user._id
      filter.lastEditDate = Date()
      filter.filteredSeats = filteredSeats
    }, upsert = true)
  }

  respondOk()
}

suspend fun AuthenticatedApplicationCall.deleteSeatFilter() {
  if (!user.isModerator) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"]!!

  val params = receiveJsonMap()
  val seat = params.getValue("seat") as Int

  runOnDb {
    getCollection<BackendSeatFilter>().deleteOne(BackendSeatFilter::locationId equal locationId, BackendSeatFilter::seat equal seat)
  }

  respondOk()
}