package webcore.datePicker

import mui.material.FormControlVariant
import kotlin.js.Date

class DatePickerConfig(
  val date: Date,
  val onChange: (date: Date, isValid: Boolean) -> Unit,
  val min: Date? = null,
  val max: Date? = null,
  val label: String? = null,
  val helperText: String? = null,
  val disabled: Boolean = false,
  val error: Boolean = false,
  val fullWidth: Boolean = false,
  val variant: FormControlVariant = FormControlVariant.standard,
) {
  class DateInputValues(var year: String, var month: String, var day: String)
}