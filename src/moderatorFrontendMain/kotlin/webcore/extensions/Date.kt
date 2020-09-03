package webcore.extensions

import kotlin.js.Date

/*
 * Note: All date operations should create a new Date and must not mutate the passed date object!
 * This ensures an easier Date-API usage, avoids bugs and produces the same effects like on the backend on Kotlin/JVM.
 */

fun Date.addDays(days: Int): Date {
  return Date(this.getTime() + (days.toDouble() * 24 * 60 * 60 * 1000))
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
  val month = this.getMonth() + 1
  val day = this.getDate()
  return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

fun Date.toInputTypeTimeValueString(): String {
  val hour = this.getHours()
  val minute = this.getMinutes()
  return "${hour.twoDigitString()}:${minute.twoDigitString()}"
}

fun Date.parse(year: Int, month: Int, day: Int): Date {
  this.setFullYear(year, month, day)
  return this
}

fun Date.setFullYear(yearValue: Int, monthValue: Int?, dayValue: Int?): Double {
  val d = this
  return js("d.setFullYear(yearValue, monthValue, dayValue)")
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
fun Date.with(year: Int? = null, month: Int? = null, day: Int? = null, hour: Int? = null, minute: Int? = null) = Date(
    year = year ?: this.getFullYear(),
    month = month ?: this.getMonth(),
    day = day ?: this.getDate(),
    hour = hour ?: this.getHours(),
    minute = minute ?: this.getMinutes()
)


/** Copied from java.util.date */
infix operator fun Date.compareTo(other: Date): Int {
  val thisTime = this.getTime()
  val anotherTime = other.getTime()
  return if (thisTime < anotherTime) -1 else (if (thisTime == anotherTime) 0 else 1)
}