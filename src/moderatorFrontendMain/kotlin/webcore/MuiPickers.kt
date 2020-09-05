import kotlinx.html.RP
import react.RClass
import react.RProps
import kotlin.js.Date

@JsModule("@material-ui/pickers")
private external val importedMuiPickers: dynamic

interface MuiPickersUtilsProviderProps : RProps {
  var utils: LuxonUtils
}

// ATTENTION!! Must wrap the root of your app
val MuiPickersUtilsProvider: RClass<MuiPickersUtilsProviderProps> = importedMuiPickers.MuiPickersUtilsProvider

interface MuiDatePickerProps : RProps {
  var label: String
  var format: String
  var value: Date
  var maxDate: Date
  var minDate: Date

  // type of the value depend on the used Util library. in our case we choosed Luxon
  var onChange: (value: LuxonDateTime) -> Unit
  var onOpen: () -> Unit
  var onClose: () -> Unit
  var animateYearScrolling: Boolean
  var autoOk: Boolean
  var disabled: Boolean
  var readOnly: Boolean
  var allowKeyboardControl: Boolean
  var open: Boolean
  var variant: String // "dialog" | "inline" | "static"
  var fullWidth: Boolean
  var inputVariant: String
}

val muiDatePicker: RClass<MuiDatePickerProps> = importedMuiPickers.DatePicker

interface MuiDateTimePickerProps : MuiDatePickerProps {
  var ampm: Boolean
  var disablePast: Boolean
}

val muiDateTimePicker: RClass<MuiDateTimePickerProps> = importedMuiPickers.DateTimePicker