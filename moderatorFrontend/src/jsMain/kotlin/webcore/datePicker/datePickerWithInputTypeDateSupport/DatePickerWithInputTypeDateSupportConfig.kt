package webcore.datePicker.datePickerWithInputTypeDateSupport

import mui.material.FormControlVariant
import webcore.TextFieldOnChange
import kotlin.js.Date

class DatePickerWithInputTypeDateSupportConfig(
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
  val dateTimeInputValue: String,
  val fieldError: Boolean,
  val newBrowsersDateTextFieldOnChange: TextFieldOnChange,
)