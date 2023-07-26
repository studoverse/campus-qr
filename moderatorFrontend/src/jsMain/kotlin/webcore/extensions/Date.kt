@file:Suppress("UNUSED_PARAMETER")

package webcore.extensions

import kotlin.js.Date

/*
 * Note: All date operations should create a new Date and must not mutate the passed date object!
 * This ensures an easier Date-API usage, avoids bugs and produces the same effects like on the backend on Kotlin/JVM.
 */

fun Date.setFullYear(yearValue: Int, monthValue: Int, dayValue: Int): Date {
  val d = Date(this.getTime())
  js("d.setFullYear(yearValue, monthValue, dayValue)")
  return d
}

fun Date.setTime(hours: Int, minutes: Int, seconds: Int, milliseconds: Int): Date {
  val t = Date(this.getTime())
  js("t.setHours(hours); t.setMinutes(minutes); t.setSeconds(seconds); t.setMilliseconds(milliseconds)")
  return t
}

fun Date.addDays(days: Int): Date {
  return Date(this.getTime() + (days.toDouble() * 24 * 60 * 60 * 1000))
}

fun Date.addYears(years: Int): Date {
  return this.with(year = this.getFullYear() + years)
}

fun Date.addHours(hours: Int): Date {
  return Date(this.getTime() + (hours.toDouble() * 60 * 60 * 1000))
}

fun Date.addMinutes(minutes: Int): Date {
  return Date(this.getTime() + (minutes.toDouble() * 60 * 1000))
}

fun Date.readableString(): String {
  val day = this.getDate().toString().padStart(2, '0')
  val month = (this.getMonth() + 1).toString().padStart(2, '0')
  return "$day.$month.${this.getFullYear()}"
}

fun Date.isInNDays(n: Int): Boolean {
  return (Date().addDays(n).toDateString() == this.toDateString())
}

fun Date.isToday(): Boolean {
  return isInNDays(0)
}

fun Date.toInputTypeDateValueString(): String {
  val year = this.getFullYear()
  val month = this.getMonth() + 1 // + 1 because month is 0 based
  val day = this.getDate()
  return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

fun Date.toInputTypeTimeValueString(): String {
  val hour = this.getHours()
  val minute = this.getMinutes()
  return "${hour.twoDigitString()}:${minute.twoDigitString()}"
}

fun Date.onSameDayAs(other: Date) =
  (this.getFullYear() == other.getFullYear() &&
      this.getDate() == other.getDate() &&
      this.getMonth() == other.getMonth())

fun Date.utcToLocalDate(): Date = Date(getTime() + getTimezoneOffset() * 60 * 1000)

fun Date.localToUtcDate(): Date = Date(getTime() - getTimezoneOffset() * 60 * 1000)

/**
 * Creates a new [Date] object with given parameters, defaults to given date.
 * @param month 0 based
 */
fun Date.with(
  year: Int? = null,
  month: Int? = null,
  day: Int? = null,
  hour: Int? = null,
  minute: Int? = null,
  second: Int? = null,
  millisecond: Int? = null
): Date {
  var date = this.setFullYear(
    year ?: this.getFullYear(),
    month ?: this.getMonth(),
    day ?: this.getDate(),
  )
  date = date.setTime(
    hour ?: this.getHours(),
    minute ?: this.getMinutes(),
    second ?: this.getSeconds(),
    millisecond ?: this.getMilliseconds()
  )
  return date
}

fun Date.startOfTheDay() = this.with(hour = 0, minute = 0, second = 0, millisecond = 0)

fun Date.endOfTheDay() = this.with(hour = 23, minute = 59, second = 59, millisecond = 999)

/** Copied from java.util.date */
infix operator fun Date.compareTo(other: Date): Int {
  val thisTime = this.getTime()
  val anotherTime = other.getTime()
  return if (thisTime < anotherTime) -1 else (if (thisTime == anotherTime) 0 else 1)
}

/** Ensures that this date is not after the specified maximumDate. */
fun Date.coerceAtMost(maximumDate: Date): Date {
  return if (this <= maximumDate) {
    Date(this.getTime())
  } else {
    Date(maximumDate.getTime())
  }
}

/** Ensures that this date is not before the specified minimumDate. */
fun Date.coerceAtLeast(minimumDate: Date): Date {
  return if (this >= minimumDate) {
    Date(this.getTime())
  } else {
    Date(minimumDate.getTime())
  }
}