package webcore.datePicker.datePickerInputField

import mui.material.FormControlVariant
import webcore.TextFieldOnChange

class DatePickerInputFieldConfig(
  val value: String,
  val label: String,
  val placeholder: String = label,
  val disabled: Boolean,
  val error: Boolean,
  val fullWidth: Boolean,
  val fieldError: Boolean,
  val variant: FormControlVariant,
  val onChange: TextFieldOnChange,
)