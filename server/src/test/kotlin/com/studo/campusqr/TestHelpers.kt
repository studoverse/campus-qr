package com.studo.campusqr

import com.moshbit.katerbase.equal
import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.Configuration
import com.studo.campusqr.database.MainDatabase
import java.util.*

internal fun createTestCheckIn(
    checkInDate: Date,
    checkOutDate: Date? = null,
    email: String = "test@test.com",
    locationId: String,
    seat: Int? = null
) = CheckIn().apply {
  _id = randomId()
  this.locationId = locationId
  date = checkInDate
  this.checkOutDate = checkOutDate
  this.email = email
  this.seat = seat
}

internal fun createTestFilter(
  locationId: String,
  seat: Int,
  filteredSeats: List<Int>
) = BackendSeatFilter().apply {
  _id = randomId()
  this.locationId = locationId
  this.seat = seat
  this.filteredSeats = filteredSeats
  lastEditDate = Date()
}

internal fun setConfig(id: String, value: String) {
  MainDatabase.getCollection<Configuration>().updateOne(Configuration::_id equal id) {
    Configuration::stringValue setTo value
  }
}

internal fun setConfig(id: String, value: Int) {
  MainDatabase.getCollection<Configuration>().updateOne(Configuration::_id equal id) {
    Configuration::intValue setTo value
  }
}

internal fun setConfig(id: String, value: Boolean) {
  MainDatabase.getCollection<Configuration>().updateOne(Configuration::_id equal id) {
    Configuration::intValue setTo if (value) 1 else 0
  }
}