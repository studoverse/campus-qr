package com.studo.campusqr

import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.endpoints.generateContactTracingReport
import com.studo.campusqr.extensions.addDays
import com.studo.campusqr.extensions.addMinutes
import com.studo.campusqr.extensions.addSeconds
import com.studo.campusqr.extensions.runOnDb
import com.studo.katerbase.MongoMainEntry
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class ContactTracingSeatTest {

  @Test
  fun testSeatFiltering() {
    setConfig("transitThresholdSeconds", 0)

    with(MainDatabase.getCollection<BackendSeatFilter>()) {
      val filter = createTestFilter(
        locationId = "testLocation1",
        seat = 1,
        filteredSeats = listOf(2, 3, 5)
      )
      insertOne(filter, upsert = true)
    }

    var report = runBlocking {
      generateContactTracingReport(emails = listOf(infectedEmail), oldestDate = now.addDays(-1))
    }

    assertEquals(
      expected = setOf("a@test.com", "b@test.com", "d@test.com"),
      actual = report.impactedUsersEmails.toSet()
    )

    assertEquals(
      report.impactedUsersCount, 3
    )

    report = runBlocking {
      generateContactTracingReport(emails = listOf(infectedEmail), oldestDate = now.addDays(-1))
    }

    with(MainDatabase.getCollection<BackendSeatFilter>()) {
      val filter = createTestFilter(
        locationId = "testLocation2",
        seat = 1,
        filteredSeats = listOf(2)
      )
      insertOne(filter, upsert = true)
    }

    assertEquals(
      expected = setOf("a@test.com", "b@test.com", "d@test.com"),
      actual = report.impactedUsersEmails.toSet()
    )

    assertEquals(
      report.impactedUsersCount, 3
    )
  }

  companion object {
    val now = Date()
    val infectedCheckIn = now.addMinutes(-60)
    val infectedCheckOut = now.addMinutes(-30)
    private const val infectedEmail = "infected@test.com"
    private const val transitUserEmail = "transitUser@test.com"

    @BeforeClass
    @JvmStatic
    fun setup() {

      with(MainDatabase.getCollection<BackendLocation>()) {
        clear()
        assertEquals(0, count())

        insertOne(BackendLocation().apply {
          _id = "testLocation1"
          name = "Test Location 1"
          createdBy = "test"
          createdDate = now.addDays(-1)
          seatCount = 20
        }, upsert = false)

        insertOne(BackendLocation().apply {
          _id = "testLocation2"
          name = "Test Location 2"
          createdBy = "test"
          createdDate = now.addDays(-1)
          seatCount = 15
        }, upsert = false)
      }

      with(MainDatabase.getCollection<CheckIn>()) {
        clear()
        assertEquals(0, count())

        // Test data is taken from function docs of generateContactTracingReport()
        bulkWrite {
          listOf(
            // Test location 1
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckIn,
              checkOutDate = infectedCheckOut,
              email = infectedEmail,
              seat = 1
            ),
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckIn.addMinutes(5),
              checkOutDate = infectedCheckOut.addMinutes(-5),
              email = "a@test.com",
              seat = 2
            ),
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckOut.addMinutes(-5),
              checkOutDate = null,
              email = "b@test.com",
              seat = 3
            ),
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckIn.addMinutes(-5),
              checkOutDate = infectedCheckIn.addMinutes(5),
              email = "c@test.com",
              seat = 4
            ),
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckOut.addMinutes(-5),
              checkOutDate = infectedCheckOut.addMinutes(5),
              email = "d@test.com",
              seat = 5
            ),
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckIn.addMinutes(-10),
              checkOutDate = infectedCheckIn.addMinutes(-5),
              email = "e@test.com",
              seat = 6
            ),
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckOut.addMinutes(5),
              checkOutDate = infectedCheckOut.addMinutes(10),
              email = "f@test.com",
              seat = 7
            ),
            createTestCheckIn(
              locationId = "testLocation1",
              checkInDate = infectedCheckIn.addMinutes(-5),
              checkOutDate = infectedCheckOut.addMinutes(5),
              email = "g@test.com",
              seat = 8
            ),
            createTestCheckIn(
              locationId = "testLocation2",
              checkInDate = infectedCheckIn,
              checkOutDate = infectedCheckOut,
              email = infectedEmail,
              seat = 1
            ),
            createTestCheckIn(
              locationId = "testLocation2",
              checkInDate = infectedCheckIn,
              checkOutDate = infectedCheckOut,
              email = "a2@test.com",
              seat = 2
            ),
            createTestCheckIn(
              locationId = "testLocation2",
              checkInDate = infectedCheckOut,
              checkOutDate = null,
              email = "b2@test.com",
              seat = 3
            ),
          ).forEach { insertOne(it, upsert = false) }
        }
      }
    }
  }
}