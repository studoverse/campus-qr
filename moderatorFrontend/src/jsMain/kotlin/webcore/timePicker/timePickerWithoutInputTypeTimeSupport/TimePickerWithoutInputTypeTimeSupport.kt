package webcore.timePicker.timePickerWithoutInputTypeTimeSupport

import com.studo.campusqr.common.utils.LocalizedString
import js.lazy.Lazy
import js.objects.unsafeJso
import mui.material.BaseTextFieldProps
import mui.material.Box
import mui.material.TextField
import mui.system.sx
import react.Props
import web.cssom.Flex
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import web.html.InputType
import webcore.FcWithCoroutineScope
import webcore.onChange
import webcore.min
import webcore.max
import util.get
import web.html.number
import webcore.toReactNode

external interface TimePickerWithoutInputTypeTimeSupportProps : Props {
  var config: TimePickerWithoutInputTypeTimeSupportConfig
}

@Lazy
val TimePickerWithoutInputTypeTimeSupport = FcWithCoroutineScope<TimePickerWithoutInputTypeTimeSupportProps> { props, launch ->
  val hourString = LocalizedString(
    "Hour",
    "Stunde"
  ).get()
  val minuteString = LocalizedString(
    "Minute",
    "Minute"
  ).get()
  Box {
    sx {
      width = 100.pct
    }
    fun BaseTextFieldProps.setCommonProps(onChange: (String) -> Unit) {
      fullWidth = props.config.fullWidth
      disabled = props.config.disabled
      error = props.config.error
      sx {
        flex = Flex(number(1.0), number(0.0))
        paddingTop = 16.px
      }
      type = InputType.number
      this.onChange = { event ->
        onChange(event.target.value)
      }
    }

    TextField {
      variant = props.config.variant
      setCommonProps(onChange = { hour ->
        props.config.tryParsingInputFields(hour, props.config.oldBrowsersInputValues.minute)
      })
      placeholder = hourString
      label = hourString.toReactNode()
      inputProps = unsafeJso {
        min = 0
        max = 23
      }
      value = props.config.oldBrowsersInputValues.hour
    }
    TextField {
      variant = props.config.variant
      setCommonProps(onChange = { minute ->
        props.config.tryParsingInputFields(props.config.oldBrowsersInputValues.hour, minute)
      })
      placeholder = minuteString
      label = minuteString.toReactNode()
      inputProps = unsafeJso {
        min = 0
        max = 59
      }
      value = props.config.oldBrowsersInputValues.minute
    }
  }
}