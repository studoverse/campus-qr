@file:Suppress("unused")

package webcore.extensions

import js.objects.jso

@Suppress("UnsafeCastFromDynamic")
fun Int.toFixed(digits: Int): String = this.asDynamic().toFixed(digits)

@Suppress("UnsafeCastFromDynamic")
fun Double.toFixed(digits: Int): String = this.asDynamic().toFixed(digits)

@Suppress("UnsafeCastFromDynamic")
fun Float.toFixed(digits: Int): String = this.asDynamic().toFixed(digits)

fun Int.toLocaleString(locale: String = "en", options: dynamic = jso<String> { }): String =
  this.asDynamic().toLocaleString(locale, options) as String

fun Int.twoDigitString(): String = toLocaleString(options = jso {
  minimumIntegerDigits = 2
  useGrouping = false
})