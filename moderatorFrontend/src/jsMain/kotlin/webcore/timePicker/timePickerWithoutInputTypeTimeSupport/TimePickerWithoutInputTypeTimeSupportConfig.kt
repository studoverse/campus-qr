package webcore.timePicker.timePickerWithoutInputTypeTimeSupport

import mui.material.FormControlVariant
import webcore.timePicker.TimePickerConfig

class TimePickerWithoutInputTypeTimeSupportConfig(
  val oldBrowsersInputValues: TimePickerConfig.Companion.TimeInputValues,
  val disabled: Boolean = false,
  val error: Boolean = false,
  val fullWidth: Boolean = false,
  val variant: FormControlVariant = FormControlVariant.standard,
  val tryParsingInputFields: (hourInputValue: String, minuteInputValue: String) -> Unit,
)