package com.studo.campusqr.database

import com.studo.campusqr.extensions.addMinutes
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
fun startAutomaticCheckOut() = serverScope.launch {
  while (true) {
    try {
      automaticCheckOut()
      delay(timeMillis = 60 * 1000L) // 1 minutes
    } catch (e: Exception) {
      println(e) // Don't crash on short-term database connection problems but make sure we still run the automaticDataDeletion
    }
  }
}

internal suspend fun automaticCheckOut() {
  val now = Date()
  runOnDb {
    val autoCheckOutMinutes: Int = getConfig("autoCheckOutMinutes")

    getCollection<CheckIn>().updateMany(
        CheckIn::checkOutDate equal null,
        CheckIn::date lower now.addMinutes(-autoCheckOutMinutes)
    ) {
      CheckIn::checkOutDate setTo now
      CheckIn::autoCheckOut setTo true
    }
  }
}