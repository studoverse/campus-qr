package webcore.timePicker

import mui.material.FormControlVariant
import kotlin.js.Date

class TimePickerConfig(
  var time: Date,
  var onChange: (date: Date) -> Unit,
  var disabled: Boolean = false,
  var error: Boolean = false,
  var min: Date? = null,
  var max: Date? = null,
  var stepMinutes: Int = 1,
  var fullWidth: Boolean = false,
  var label: String? = null,
  var helperText: String? = null,
  var variant: FormControlVariant = FormControlVariant.outlined,
) {
  companion object {
    class TimeInputValues(var hour: String, var minute: String)
  }
}
