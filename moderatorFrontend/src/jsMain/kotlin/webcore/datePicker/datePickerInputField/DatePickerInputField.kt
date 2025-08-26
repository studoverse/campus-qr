package webcore.datePicker.datePickerInputField

import webcore.datePicker.DatePickerUtils.textFieldLabelStyle
import js.objects.unsafeJso
import mui.material.TextField
import mui.material.textFieldClasses
import mui.system.sx
import react.Props
import web.cssom.Flex
import web.cssom.number
import web.html.InputType
import web.html.number
import webcore.FcWithCoroutineScope
import webcore.onChange
import webcore.min
import webcore.max
import webcore.toReactNode

external interface DatePickerInputFieldProps : Props {
  var config: DatePickerInputFieldConfig
}

val DatePickerInputField = FcWithCoroutineScope<DatePickerInputFieldProps> { props, launch ->
  TextField {
    sx {
      flex = Flex(number(1.0), number(0.0))
      textFieldClasses.root {
        textFieldLabelStyle()
      }
    }
    type = InputType.number
    fullWidth = props.config.fullWidth
    disabled = props.config.disabled
    error = props.config.error || props.config.fieldError
    variant = props.config.variant
    this.onChange = props.config.onChange
    this.placeholder = props.config.placeholder
    this.label = props.config.label.toReactNode()
    inputProps = unsafeJso {
      min = 1
      max = 31
    }
    this.value = props.config.value
  }
}