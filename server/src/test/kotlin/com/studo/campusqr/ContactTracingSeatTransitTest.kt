package com.studo.campusqr

import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.endpoints.generateContactTracingReport
import com.studo.campusqr.extensions.addDays
import com.studo.campusqr.extensions.addMinutes
import com.studo.campusqr.extensions.addSeconds
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class ContactTracingSeatTransitTest {
  @Test
  fun testContactTracing() {
    setConfig("transitThresholdSeconds", 0)

    val report = runBlocking {
      generateContactTracingReport(emails = listOf(infectedEmail), oldestDate = now.addDays(-1))
    }

    assertEquals(
      expected = setOf("a@test.com", "b@test.com", "c@test.com", "d@test.com", "g@test.com"),
      actual = report.impactedUsersEmails.toSet()
    )
  }

  @Test
  fun testTransitThreshold() {
    setConfig("transitThresholdSeconds", 60)

    var report = runBlocking {
      generateContactTracingReport(emails = listOf(infectedEmail), oldestDate = now.addDays(-1))
    }
    assertEquals(true, actual = transitUserEmail !in report.impactedUsersEmails.toSet())

    setConfig("transitThresholdSeconds", 120)

    report = runBlocking {
      generateContactTracingReport(emails = listOf(infectedEmail), oldestDate = now.addDays(-1))
    }
    assertEquals(true, actual = transitUserEmail in report.impactedUsersEmails.toSet())
  }

  companion object {
    val now = Date()
    private const val infectedEmail = "infected@test.com"
    private const val transitUserEmail = "transitUser@test.com"

    @BeforeClass
    @JvmStatic
    fun setup() {
      val infectedCheckIn = now.addMinutes(-60)
      val infectedCheckOut = now.addMinutes(-30)
      val location = "testLocation"

      with(MainDatabase.getCollection<BackendLocation>()) {
        clear()
        assertEquals(0, count())

        insertOne(BackendLocation().apply {
          _id = location
          name = "Test Location"
          createdBy = "test"
          createdDate = now.addDays(-1)
          seatCount = 10
        }, upsert = false)
      }

      with(MainDatabase.getCollection<BackendSeatFilter>()) {
        clear()
        assertEquals(0, count())

        insertOne(
          createTestFilter(
            locationId = location,
            seat = 1,
            filteredSeats = listOf(2, 3)
          ), upsert = true
        )
        assertEquals(1, count())
      }

      with(MainDatabase.getCollection<CheckIn>()) {
        clear()
        assertEquals(0, count())

        // Test data is taken from function docs of generateContactTracingReport()
        bulkWrite {
          listOf(
            createTestCheckIn(
              checkInDate = infectedCheckIn,
              checkOutDate = infectedCheckOut,
              email = infectedEmail,
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckIn.addMinutes(5),
              checkOutDate = infectedCheckOut.addMinutes(-5),
              email = "a@test.com",
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckOut.addMinutes(-5),
              checkOutDate = null,
              email = "b@test.com",
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckIn.addMinutes(-5),
              checkOutDate = infectedCheckIn.addMinutes(5),
              email = "c@test.com",
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckOut.addMinutes(-5),
              checkOutDate = infectedCheckOut.addMinutes(5),
              email = "d@test.com",
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckIn.addMinutes(-10),
              checkOutDate = infectedCheckIn.addMinutes(-5),
              email = "e@test.com",
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckOut.addMinutes(5),
              checkOutDate = infectedCheckOut.addMinutes(10),
              email = "f@test.com",
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckIn.addMinutes(-5),
              checkOutDate = infectedCheckOut.addMinutes(5),
              email = "g@test.com",
              seat = 1,
              locationId = location
            ),
            createTestCheckIn(
              checkInDate = infectedCheckIn.addMinutes(-15),
              checkOutDate = infectedCheckIn.addSeconds(-100),
              email = transitUserEmail,
              seat = 1,
              locationId = location
            )
          ).forEach { insertOne(it, upsert = false) }
        }
      }
    }
  }
}