import kotlin.js.Date

@JsModule("@date-io/luxon")
private external val importedLuxonUtils: dynamic

external class LuxonUtils

val luxonUtils: LuxonUtils = importedLuxonUtils.default

@JsModule("luxon")
private external val importedLuxon: dynamic
val luxonDateTime: LuxonDateTime = importedLuxon.DateTime

// full reference of Luxon DateTime class can be found here:
// https://moment.github.io/luxon/docs/class/src/datetime.js~DateTime.html
external class LuxonDateTime {
  val day: Number get() = definedExternally
  val hour: Number get() = definedExternally
  val second: Number get() = definedExternally
  val year: Number get() = definedExternally
  val weekday: Number get() = definedExternally

  fun toJSDate(): Date = definedExternally

  @JsName("ts")
  val time: Number
    get() = definedExternally
}

fun LuxonDateTime.toNativeJsDate(): Date = this.unsafeCast<Date>()