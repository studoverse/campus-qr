package webcore.extensions

import kotlinext.js.js

@Suppress("UnsafeCastFromDynamic")
fun Int.toFixed(digits: Int): String = this.asDynamic().toFixed(digits)

@Suppress("UnsafeCastFromDynamic")
fun Double.toFixed(digits: Int): String = this.asDynamic().toFixed(digits)

@Suppress("UnsafeCastFromDynamic")
fun Float.toFixed(digits: Int): String = this.asDynamic().toFixed(digits)

fun Int.toLocaleString(locale: String = "en", options: dynamic = js { }): String =
    this.asDynamic().toLocaleString(locale, options) as String

fun Int.twoDigitString(): String = toLocaleString(options = js {
  minimumIntegerDigits = 2
  useGrouping = false
})