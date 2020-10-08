package com.studo.campusqr

import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.Configuration
import com.studo.campusqr.database.MainDatabase
import com.studo.katerbase.equal
import java.util.*

internal fun createTestCheckIn(
    checkInDate: Date,
    checkOutDate: Date? = null,
    email: String = "test@test.com"
) = CheckIn().apply {
  _id = randomId()
  locationId = "testLocation"
  date = checkInDate
  this.checkOutDate = checkOutDate
  this.email = email
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