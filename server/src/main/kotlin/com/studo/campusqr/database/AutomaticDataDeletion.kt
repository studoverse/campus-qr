package com.studo.campusqr.database

import com.moshbit.katerbase.greaterEquals
import com.moshbit.katerbase.lower
import com.moshbit.katerbase.none
import com.studo.campusqr.extensions.addDays
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.serverScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * This background task deletes old [CheckIn]s after a defined amount of days
 */
fun startAutomaticDataDeletion() = serverScope.launch {
  while (true) {
    try {
      automaticDataDeletion()
      delay(timeMillis = 10 * 60 * 1000L) // 10 minutes
    } catch (e: Exception) {
      println(e) // Don't crash on short-term database connection problems but make sure we still run the automaticDataDeletion
    }
  }
}

internal suspend fun automaticDataDeletion() {
  val deleteDays: Int = runOnDb { getConfig("deleteCheckInDataAfterDays") }

  val now = Date()
  val deleteResult = runOnDb {
    getCollection<SessionToken>().deleteMany(SessionToken::expiryDate lower now)
    getCollection<CheckIn>().deleteMany(CheckIn::date lower now.addDays(-deleteDays))
    getCollection<BackendAccess>().deleteMany(
      BackendAccess::dateRanges.none(
        BackendDateRange::to greaterEquals now.addDays(-deleteDays)
      )
    )
  }
  println("Deleted ${deleteResult.deletedCount} check-ins that were older than $deleteDays days.")
}

