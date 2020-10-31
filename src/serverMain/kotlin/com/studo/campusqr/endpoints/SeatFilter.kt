package com.studo.campusqr.endpoints

import com.studo.campusqr.common.DeleteSeatFilter
import com.studo.campusqr.common.EditSeatFilter
import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.extensions.receiveClientPayload
import com.studo.campusqr.extensions.respondForbidden
import com.studo.campusqr.extensions.respondOk
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import com.studo.katerbase.MongoMainEntry.Companion.generateId
import com.studo.katerbase.equal
import io.ktor.features.*
import java.util.*

/**
 * This file contains endpoints which are used in to modify location seat filters.
 */

// Edit or create seat filter
suspend fun AuthenticatedApplicationCall.editSeatFilter() {
  if (!user.canEditLocations) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"] ?: throw BadRequestException("No locationId provided")
  val location = getLocation(locationId)

  val params: EditSeatFilter = receiveClientPayload()

  // Validate seat & filteredSeats argument
  when {
    location.seatCount == null -> {
      throw BadRequestException("SeatFilter can not be inserted on location with no seats")
    }
    params.seat <= 0 -> {
      throw BadRequestException("Seat must be > 0")
    }
    params.seat > location.seatCount!! -> {
      throw BadRequestException("Seat must be <= location.seatCount")
    }
    params.filteredSeats.any { it <= 0 } -> {
      throw BadRequestException("FilteredSeats must all be > 0")
    }
    params.filteredSeats.any { it > location.seatCount!! } -> {
      throw BadRequestException("FilteredSeats must all be <= location.seatCount")
    }
    else -> {
      runOnDb {
        getCollection<BackendSeatFilter>().insertOne(BackendSeatFilter().also { filter ->
          filter._id = generateId(locationId, params.seat.toString())
          filter.locationId = locationId
          filter.seat = params.seat
          filter.editedBy = user._id
          filter.lastEditDate = Date()
          filter.filteredSeats = params.filteredSeats
        }, upsert = true)
      }

      respondOk()
    }
  }
}

suspend fun AuthenticatedApplicationCall.deleteSeatFilter() {
  if (!user.canEditLocations) {
    respondForbidden()
    return
  }

  val locationId = parameters["id"] ?: throw BadRequestException("No locationId provided")

  val params: DeleteSeatFilter = receiveClientPayload()

  runOnDb {
    getCollection<BackendSeatFilter>()
      .deleteOne(BackendSeatFilter::locationId equal locationId, BackendSeatFilter::seat equal params.seat)
  }

  respondOk()
}