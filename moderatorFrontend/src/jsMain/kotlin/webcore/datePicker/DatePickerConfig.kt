package webcore.datePicker

import mui.material.FormControlVariant
import kotlin.js.Date

class DatePickerConfig(
  var date: Date,
  var onChange: (date: Date, isValid: Boolean) -> Unit,
  var disabled: Boolean = false,
  var error: Boolean = false,
  var min: Date? = null,
  var max: Date? = null,
  var fullWidth: Boolean = false,
  var label: String? = null,
  var helperText: String? = null,
  var variant: FormControlVariant = FormControlVariant.outlined,
)
