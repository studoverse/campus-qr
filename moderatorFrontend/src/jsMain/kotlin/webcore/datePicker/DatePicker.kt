package webcore.datePicker

import webcore.datePicker.datePickerWithInputTypeDateSupport.DatePickerWithInputTypeDateSupport
import webcore.datePicker.datePickerWithInputTypeDateSupport.DatePickerWithInputTypeDateSupportConfig
import webcore.datePicker.datePickerWithoutInputTypeDateSupport.DatePickerWithoutInputTypeDateSupport
import webcore.datePicker.datePickerWithoutInputTypeDateSupport.DatePickerWithoutInputTypeDateSupportConfig
import mui.material.Box
import mui.system.sx
import react.Props
import web.cssom.pct
import webcore.FcWithCoroutineScope

external interface DatePickerProps : Props {
  var config: DatePickerConfig
}

val DatePicker = FcWithCoroutineScope<DatePickerProps> { props, launch ->
  val controller = DatePickerController.use(launch = launch, props = props)

  Box {
    if (props.config.fullWidth) {
      sx {
        width = 100.pct
      }
    }
    if (controller.detectInputDateSupport()) {
      DatePickerWithInputTypeDateSupport {
        config = DatePickerWithInputTypeDateSupportConfig(
          date = props.config.date,
          min = props.config.min,
          max = props.config.max,
          helperText = props.config.helperText,
          label = props.config.label,
          fullWidth = props.config.fullWidth,
          disabled = props.config.disabled,
          error = props.config.error,
          onChange = props.config.onChange,
          dateTimeInputValue = controller.dateTimeInputValue,
          fieldError = controller.fieldError,
          newBrowsersDateTextFieldOnChange = controller.newBrowsersDateTextFieldOnChange,
        )
      }
    } else {
      DatePickerWithoutInputTypeDateSupport {
        config = DatePickerWithoutInputTypeDateSupportConfig(
          date = props.config.date,
          min = props.config.min,
          max = props.config.max,
          helperText = props.config.helperText,
          label = props.config.label,
          fullWidth = props.config.fullWidth,
          disabled = props.config.disabled,
          error = props.config.error,
          onChange = props.config.onChange,
          fieldError = controller.fieldError,
          oldBrowsersDayTextFieldOnChange = controller.oldBrowsersDayTextFieldOnChange,
          oldBrowsersMonthTextFieldOnChange = controller.oldBrowsersMonthTextFieldOnChange,
          oldBrowsersYearTextFieldOnChange = controller.oldBrowsersYearTextFieldOnChange,
          tryParsingInputFields = controller.tryParsingInputFields,
          variant = props.config.variant,
          oldBrowsersInputValues = controller.oldBrowsersInputValues,
        )
      }
    }
  }
}
