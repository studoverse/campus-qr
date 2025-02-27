package webcore.timePicker.timePickerWithInputTypeTimeSupport

import mui.material.FormControlVariant
import kotlin.js.Date

class TimePickerWithInputTypeTimeSupportConfig(
  val time: Date,
  val onChange: (date: Date) -> Unit,
  val min: Date? = null,
  val max: Date? = null,
  val stepMinutes: Int = 1,
  val disabled: Boolean = false,
  val error: Boolean = false,
  val fullWidth: Boolean = false,
  val label: String? = null,
  val helperText: String? = null,
  val variant: FormControlVariant = FormControlVariant.standard,
)