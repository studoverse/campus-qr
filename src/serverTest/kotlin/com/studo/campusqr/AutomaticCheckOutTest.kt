package com.studo.campusqr

import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.automaticCheckOut
import com.studo.campusqr.extensions.addMinutes
import com.studo.katerbase.greater
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class AutomaticCheckOutTest {
  private fun createTestCheckIn(checkInDate: Date, checkOutDate: Date?) = CheckIn().apply {
    _id = randomId()
    locationId = "testLocation"
    date = checkInDate
    this.checkOutDate = checkOutDate
    email = "test@test.com"
  }

  @Test
  fun checkOutTest() {
    val now = Date()

    val autoCheckOutMinutes = MainDatabase.getConfig<Int>("autoCheckOutMinutes")
    val checkInDate = Date().addMinutes(-autoCheckOutMinutes)

    with(MainDatabase.getCollection<CheckIn>()) {
      clear()
      assertEquals(0, count())

      insertOne(
          document = createTestCheckIn(checkInDate, checkOutDate = null),
          upsert = false
      )

      assertEquals(1, count())
      assertEquals(null, find(CheckIn::checkOutDate greater now).firstOrNull()?.date)

      runBlocking { automaticCheckOut() }

      assertEquals(checkInDate, find(CheckIn::checkOutDate greater now).firstOrNull()?.date)
    }
  }
}