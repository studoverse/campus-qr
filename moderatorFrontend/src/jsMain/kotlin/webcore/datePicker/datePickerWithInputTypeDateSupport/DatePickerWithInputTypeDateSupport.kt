package webcore.datePicker.datePickerWithInputTypeDateSupport

import js.objects.unsafeJso
import mui.material.TextField
import mui.system.sx
import react.Props
import web.cssom.WhiteSpace
import web.html.InputType
import web.html.date
import webcore.FcWithCoroutineScope
import webcore.extensions.toInputTypeDateValueString
import webcore.toReactNode
import webcore.onChange
import webcore.min
import webcore.max

external interface DatePickerWithInputTypeDateSupportProps : Props {
  var config: DatePickerWithInputTypeDateSupportConfig
}

val DatePickerWithInputTypeDateSupport = FcWithCoroutineScope<DatePickerWithInputTypeDateSupportProps> { props, launch ->
  TextField {
    variant = props.config.variant
    type = InputType.date
    value = props.config.dateTimeInputValue
    inputProps = unsafeJso {
      props.config.min?.let { minProp ->
        min = minProp.toInputTypeDateValueString()
      }
      props.config.max?.let { maxProp ->
        max = maxProp.toInputTypeDateValueString()
      }
    }
    if (props.config.helperText != null) {
      FormHelperTextProps = unsafeJso {
        sx {
          whiteSpace = WhiteSpace.preLine // Handle \n
        }
      }
    }
    label = props.config.label?.toReactNode()
    helperText = props.config.helperText?.toReactNode()
    fullWidth = props.config.fullWidth
    disabled = props.config.disabled
    error = props.config.error || props.config.fieldError
    onChange = props.config.newBrowsersDateTextFieldOnChange
  }
}