package com.studo.campusqr

import com.moshbit.katerbase.greater
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.database.automaticCheckOut
import com.studo.campusqr.extensions.addMinutes
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class AutomaticCheckOutTest {

  @Test
  fun checkOutTest() {
    val now = Date()

    val autoCheckOutMinutes = MainDatabase.getConfig<Int>("autoCheckOutMinutes")
    val checkInDate = Date().addMinutes(-autoCheckOutMinutes)

    with(MainDatabase.getCollection<CheckIn>()) {
      clear()
      assertEquals(0, count())

      insertOne(
          document = createTestCheckIn(checkInDate, checkOutDate = null, locationId = "testLocation"),
          upsert = false
      )

      assertEquals(1, count())
      assertEquals(null, find(CheckIn::checkOutDate greater now).firstOrNull()?.date)

      runBlocking { automaticCheckOut() }

      assertEquals(checkInDate, find(CheckIn::checkOutDate greater now).firstOrNull()?.date)
    }
  }
}