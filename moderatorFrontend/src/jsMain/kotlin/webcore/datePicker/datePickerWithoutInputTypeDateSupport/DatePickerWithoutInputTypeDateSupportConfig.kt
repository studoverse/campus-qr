package webcore.datePicker.datePickerWithoutInputTypeDateSupport

import mui.material.FormControlVariant
import webcore.TextFieldOnChange
import webcore.datePicker.DatePickerConfig.DateInputValues
import kotlin.js.Date

class DatePickerWithoutInputTypeDateSupportConfig(
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
  val oldBrowsersInputValues: DateInputValues,
  val fieldError: Boolean,
  val oldBrowsersDayTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersMonthTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersYearTextFieldOnChange: TextFieldOnChange,
  val tryParsingInputFields: (dayInputValue: String, monthInputValue: String, yearInputValue: String) -> Unit,
)