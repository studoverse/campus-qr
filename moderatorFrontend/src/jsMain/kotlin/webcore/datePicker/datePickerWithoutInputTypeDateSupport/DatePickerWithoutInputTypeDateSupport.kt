package webcore.datePicker.datePickerWithoutInputTypeDateSupport

import com.studo.campusqr.common.utils.LocalizedString
import mui.material.Box
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import web.cssom.Display
import web.cssom.pct
import webcore.FcWithCoroutineScope
import webcore.TextFieldOnChange
import webcore.datePicker.datePickerInputField.DatePickerInputField
import webcore.datePicker.datePickerInputField.DatePickerInputFieldConfig
import util.get

external interface DatePickerWithoutInputTypeDateSupportProps : Props {
  var config: DatePickerWithoutInputTypeDateSupportConfig
}

val DatePickerWithoutInputTypeDateSupport = FcWithCoroutineScope<DatePickerWithoutInputTypeDateSupportProps> { props, launch ->
  Box {
    sx {
      display = Display.flex
      width = 100.pct
    }

    fun ChildrenBuilder.datePickerInputField(
      value: String,
      label: String,
      onChange: TextFieldOnChange,
    ) {
      DatePickerInputField {
        config = DatePickerInputFieldConfig(
          value = value,
          label = label,
          onChange = onChange,
          disabled = props.config.disabled,
          error = props.config.error,
          fullWidth = props.config.fullWidth,
          fieldError = props.config.fieldError,
          variant = props.config.variant,
        )
      }
    }

    datePickerInputField(
      value = props.config.oldBrowsersInputValues.day,
      label = LocalizedString(
        "Day",
        "Tag"
      ).get(),
      onChange = props.config.oldBrowsersDayTextFieldOnChange,
    )

    datePickerInputField(
      value = props.config.oldBrowsersInputValues.month,
      label = LocalizedString(
        "Month",
        "Monat"
      ).get(),
      onChange = props.config.oldBrowsersMonthTextFieldOnChange,
    )

    datePickerInputField(
      value = props.config.oldBrowsersInputValues.year,
      label = LocalizedString(
        "Year",
        "Jahr"
      ).get(),
      onChange = props.config.oldBrowsersYearTextFieldOnChange,
    )
  }
}