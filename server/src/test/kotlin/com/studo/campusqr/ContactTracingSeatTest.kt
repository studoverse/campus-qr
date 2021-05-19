package com.studo.campusqr

import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.BackendSeatFilter
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.endpoints.generateContactTracingReport
import com.studo.campusqr.extensions.addDays
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class ContactTracingSeatTest {

  @Test
  fun withoutSeatFilter() {
    with(MainDatabase.getCollection<BackendSeatFilter>()) {
      clear()
      assertEquals(0, count())
    }

    val report = runBlocking {
      generateContactTracingReport(emails = listOf(infected), oldestDate = now.addDays(-7))
    }

    assertEquals(
      expected = setOf("e", "f", "g"),
      actual = report.impactedUsersEmails.toSet()
    )
  }

  @Test
  fun seatFilterInLocation1() {
    with(MainDatabase.getCollection<BackendSeatFilter>()) {
      clear()
      assertEquals(0, count())
      insertOne(
        createTestFilter(
          locationId = location1,
          seat = 2,
          filteredSeats = listOf(1, 3)
        ), upsert = true
      )
      assertEquals(1, count())
    }

    val report = runBlocking {
      generateContactTracingReport(emails = listOf(infected), oldestDate = now.addDays(-7))
    }

    assertEquals(
      expected = setOf("e", "f"),
      actual = report.impactedUsersEmails.toSet()
    )
  }

  @Test
  fun notMatchingSeatFilterInLocation1() {
    with(MainDatabase.getCollection<BackendSeatFilter>()) {
      clear()
      assertEquals(0, count())
      insertOne(
        createTestFilter(
          locationId = location1,
          seat = 1,
          filteredSeats = listOf(2, 3)
        ), upsert = true
      )
      assertEquals(1, count())
    }

    val report = runBlocking {
      generateContactTracingReport(emails = listOf(infected), oldestDate = now.addDays(-7))
    }

    assertEquals(
      expected = setOf("e", "f", "g"),
      actual = report.impactedUsersEmails.toSet()
    )
  }

  @Test
  fun seatFilterInLocation2() {
    with(MainDatabase.getCollection<BackendSeatFilter>()) {
      clear()
      assertEquals(0, count())
      insertOne(
        createTestFilter(
          locationId = location2,
          seat = 2,
          filteredSeats = listOf(1, 3)
        ), upsert = true
      )
      assertEquals(1, count())
    }

    val report = runBlocking {
      generateContactTracingReport(emails = listOf(infected), oldestDate = now.addDays(-7))
    }

    assertEquals(
      expected = setOf("e", "f", "g"),
      actual = report.impactedUsersEmails.toSet()
    )
  }

  companion object {
    val now: Date = Date()
    private val hour1: Date = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 1) }.time
    private val hour2: Date = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 2) }.time
    private val hour3: Date = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 3) }.time
    private val hour4: Date = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 4) }.time
    private val hour5: Date = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 5) }.time
    private val hour6: Date = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 6) }.time

    private const val infected = "infected"
    private const val location1 = "location1"
    private const val location2 = "location2"

    @BeforeClass
    @JvmStatic
    fun setup() {
      setConfig("transitThresholdSeconds", 0)

      with(MainDatabase.getCollection<BackendLocation>()) {
        clear()
        assertEquals(0, count())

        insertOne(BackendLocation().apply {
          _id = location1
          name = "Test Location 1"
          createdBy = "test"
          createdDate = now.addDays(-1)
          seatCount = 4
        }, upsert = false)

        insertOne(BackendLocation().apply {
          _id = location2
          name = "Test Location 2"
          createdBy = "test"
          createdDate = now.addDays(-1)
          seatCount = 4
        }, upsert = false)
      }

      with(MainDatabase.getCollection<CheckIn>()) {
        clear()
        assertEquals(0, count())

        // Test data is taken from function docs of generateContactTracingReport()
        bulkWrite {
          listOf(
            // Test location 1 / time 1:00 - 2:00
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour1,
              checkOutDate = hour2,
              email = "a",
              seat = 1
            ),
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour1,
              checkOutDate = hour2,
              email = "b",
              seat = 2
            ),
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour1,
              checkOutDate = hour2,
              email = "c",
              seat = 3
            ),
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour1,
              checkOutDate = hour2,
              email = "d",
              seat = 4
            ),
            // Test location 1 / time 3:00 - 4:00
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = "e",
              seat = 1
            ),
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = infected,
              seat = 2
            ),
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = "f",
              seat = 3
            ),
            createTestCheckIn(
              locationId = location1,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = "g",
              seat = 4
            ),
            // Location 2 / time 3:00 - 4:00
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = "h",
              seat = 1
            ),
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = "i",
              seat = 2
            ),
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = "j",
              seat = 3
            ),
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour3,
              checkOutDate = hour4,
              email = "k",
              seat = 4
            ),
            // Location 1 / time 5:00 - 6:00
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour5,
              checkOutDate = hour6,
              email = "l",
              seat = 1
            ),
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour5,
              checkOutDate = hour6,
              email = "m",
              seat = 2
            ),
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour5,
              checkOutDate = hour6,
              email = "n",
              seat = 3
            ),
            createTestCheckIn(
              locationId = location2,
              checkInDate = hour5,
              checkOutDate = hour6,
              email = "o",
              seat = 4
            ),
          ).forEach { insertOne(it, upsert = false) }
        }
      }
    }
  }
}