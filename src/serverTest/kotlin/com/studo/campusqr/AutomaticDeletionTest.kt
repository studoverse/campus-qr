package com.studo.campusqr

import com.studo.campusqr.database.*
import com.studo.campusqr.extensions.addDays
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class AutomaticDeletionTest {
  @Test
  fun testSessionTokenDeletion() {
    val testUserId = "testuser"

    val now = Date()
    val past = now.addDays(-7)
    val future = now.addDays(7)

    with(MainDatabase.getCollection<SessionToken>()) {
      clear()
      assertEquals(0, count())

      val sessionTokens = listOf(
          SessionToken().apply {
            _id = randomId()
            userId = testUserId
            expiryDate = past
          },
          SessionToken().apply {
            _id = randomId()
            userId = testUserId
            expiryDate = future
          }
      )

      sessionTokens.forEach { insertOne(it, upsert = false) }

      assertEquals(sessionTokens.count().toLong(), count())

      runBlocking { automaticDataDeletion() }
    }

    with(MainDatabase.getCollection<SessionToken>()) {
      assertEquals(1, count())

      assertEquals(future, find().single().expiryDate)
    }
  }

  @Test
  fun testCheckInDeletion() {
    fun createTestCheckIn(date: Date) = CheckIn().apply {
      _id = randomId()
      locationId = "testLocation"
      this.date = date
      email = "test@test.com"
      userAgent = ""
    }

    val now = Date()
    val oneWeekAgo = now.addDays(-7)
    val twoMonthAgo = now.addDays(-(2 * 30))

    with(MainDatabase.getCollection<CheckIn>()) {
      clear()
      assertEquals(0, count())

      val checkIns = listOf(
          createTestCheckIn(oneWeekAgo),
          createTestCheckIn(twoMonthAgo)
      )

      checkIns.forEach { insertOne(it, upsert = false) }

      assertEquals(checkIns.count().toLong(), count())

      runBlocking { automaticDataDeletion() }
    }

    with(MainDatabase.getCollection<CheckIn>()) {
      assertEquals(1, count())

      assertEquals(oneWeekAgo, find().single().date)
    }
  }

  @Test
  fun testAccessDeletion() {
    val now = Date()

    fun createTestAccess(allowedEmails: List<String>, dateRanges: List<DateRange>, createdDate: Date = now) = BackendAccess().apply {
      _id = randomId()
      locationId = "testLocation"
      createdBy = "testUser"
      this.createdDate = createdDate
      this.allowedEmails = allowedEmails
      this.dateRanges = dateRanges
      note = ""
      reason = ""
    }

    val twoMonthAgo = now.addDays(-(2 * 30))
    val threeMonthAgo = now.addDays(-(3 * 30))
    val future = now.addDays(7)

    val testEmails = listOf("test@test.com")

    with(MainDatabase.getCollection<BackendAccess>()) {
      clear()
      assertEquals(0, count())

      val accesses = listOf(
          // To Date is in the future
          createTestAccess(
              allowedEmails = testEmails,
              dateRanges = listOf(DateRange(from = twoMonthAgo, to = future)),
              createdDate = twoMonthAgo
          ),
          // To Date is in the past
          createTestAccess(
              allowedEmails = testEmails,
              dateRanges = listOf(DateRange(from = threeMonthAgo, to = twoMonthAgo)),
              createdDate = twoMonthAgo
          ),
      )

      accesses.forEach { insertOne(it, upsert = false) }
      assertEquals(accesses.count().toLong(), count())

      runBlocking { automaticDataDeletion() }
    }

    with(MainDatabase.getCollection<BackendAccess>()) {
      assertEquals(1, count())

      assertEquals(future, find().single().dateRanges.single().to)
    }
  }
}