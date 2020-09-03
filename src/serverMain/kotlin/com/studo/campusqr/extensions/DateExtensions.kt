package com.studo.campusqr.extensions

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

val austriaTimeZone: TimeZone = TimeZone.getTimeZone("Europe/Vienna")
val austriaTimeZoneId: ZoneId = TimeZone.getTimeZone("Europe/Vienna").toZoneId()

val utcTimeZone: TimeZone = TimeZone.getTimeZone("UTC")

fun Date.toIsoString(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.GERMANY).apply { timeZone = utcTimeZone }.format(this)

fun Date.toAustrianTime(pattern: String): String {
  return SimpleDateFormat(pattern).apply { timeZone = austriaTimeZone }.format(this)
}

fun Date.toAustrianTime(yearAtBeginning: Boolean = true): String {
  val pattern = if (yearAtBeginning) "yyyy-MM-dd HH:mm:ss" else "dd.MM.yyyy HH:mm:ss"
  return this.toAustrianTime(pattern)
}

fun Date.addWeeks(weeks: Int): Date = addDays(weeks * 7)

fun Date.addDays(days: Int): Date {
  val cal = Calendar.getInstance(utcTimeZone)
  cal.time = this
  cal.add(Calendar.DATE, days)
  return cal.time
}

fun Date.addHours(hours: Int): Date {
  val cal = Calendar.getInstance(utcTimeZone)
  cal.time = this
  cal.add(Calendar.HOUR, hours)
  return cal.time
}

fun Date.addSeconds(seconds: Int): Date {
  val cal = Calendar.getInstance(utcTimeZone)
  cal.time = this
  cal.add(Calendar.SECOND, seconds)
  return cal.time
}

fun Date.addMinutes(minutes: Int): Date {
  if (minutes == 0) return this

  val cal = Calendar.getInstance(utcTimeZone)
  cal.time = this
  cal.add(Calendar.MINUTE, minutes)
  return cal.time
}

fun Date.addMonths(month: Int): Date {
  val cal = Calendar.getInstance(utcTimeZone)
  cal.time = this
  cal.add(Calendar.MONTH, month)
  return cal.time
}

fun Date.addYears(year: Int): Date {
  val cal = Calendar.getInstance(utcTimeZone)
  cal.time = this
  cal.add(Calendar.YEAR, year)
  return cal.time
}