package com.studo.campusqr.database

import com.studo.campusqr.extensions.addDays
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.serverScope
import com.studo.katerbase.equal
import com.studo.katerbase.lower
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * This background task deletes old [CheckIn]s after a defined amount of days
 */
fun automaticDataDeletion() = serverScope.launch {
  while (true) {
    try {
      val deleteDays = runOnDb {
        getCollection<Configuration>().findOne(Configuration::_id equal "deleteCheckInDataAfterDays")!!.intValue!!
      }

      val deleteResult = runOnDb {
        MainDatabase.getCollection<SessionToken>().deleteMany(SessionToken::expiryDate lower Date())
        MainDatabase.getCollection<CheckIn>().deleteMany(CheckIn::date lower Date().addDays(-deleteDays))
      }

      println("Deleted ${deleteResult.deletedCount} check-ins that were older than $deleteDays days.")

      delay(timeMillis = 10 * 60 * 1000L) // 10 minutes

    } catch (e: Exception) {
      println(e) // Don't crash on short-term database connection problems but make sure we still run the automaticDataDeletion
    }
  }
}