package webcore.datePicker

import com.studo.campusqr.common.utils.LocalizedString
import js.lazy.Lazy
import web.cssom.*
import js.objects.jso
import mui.material.Box
import mui.material.TextField
import mui.system.sx
import react.*
import util.get
import web.html.InputType
import webcore.FcWithCoroutineScope
import webcore.extensions.toInputTypeDateValueString
import webcore.max
import webcore.min
import webcore.onChange
import webcore.toReactNode

external interface DatePickerProps : Props {
  var config: DatePickerConfig
}

@Lazy
val DatePicker = FcWithCoroutineScope<DatePickerProps> { props, launch ->
  val controller: DatePickerController = DatePickerController.use(config = props.config, launch = launch)

  fun ChildrenBuilder.renderWithInputTypeDateSupport() {
    TextField {
      type = InputType.date
      value = controller.dateTimeInputValue
      inputProps = jso {
        props.config.min?.let { minProp ->
          min = minProp.toInputTypeDateValueString()
        }
        props.config.max?.let { maxProp ->
          max = maxProp.toInputTypeDateValueString()
        }
      }
      props.config.label?.let { label = it.toReactNode() }
      props.config.helperText?.let { helperText = it.toReactNode() }
      fullWidth = props.config.fullWidth
      variant = props.config.variant
      disabled = props.config.disabled
      error = props.config.error || controller.fieldError

      onChange = controller.newBrowsersDateTextFieldOnChange
    }
  }

  fun ChildrenBuilder.renderWithoutInputTypeDateSupport() {
    val dayString = LocalizedString(
      "Day",
      "Tag"
    ).get()
    val monthString = LocalizedString(
      "Month",
      "Monat"
    ).get()
    val yearString = LocalizedString(
      "Year",
      "Jahr"
    ).get()
    Box.create {
      sx {
        display = Display.flex
        width = 100.pct
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error || controller.fieldError
        sx {
          flex = number(1.0)
          paddingTop = 16.px
        }
        type = InputType.number
        placeholder = dayString
        label = dayString.toReactNode()
        inputProps = jso {
          min = 1
          max = 31
        }
        value = controller.oldBrowsersInputValues.day
        onChange = controller.oldBrowsersDayTextFieldOnChange
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error || controller.fieldError
        sx {
          flex = number(2.0)
          paddingTop = 16.px
        }
        type = InputType.number
        placeholder = monthString
        label = monthString.toReactNode()
        inputProps = jso {
          min = 1
          max = 12
        }
        value = controller.oldBrowsersInputValues.month
        onChange = controller.oldBrowsersMonthTextFieldOnChange
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error || controller.fieldError
        sx {
          flex = number(2.0)
          paddingTop = 16.px
        }
        type = InputType.number
        placeholder = yearString
        label = yearString.toReactNode()
        value = controller.oldBrowsersInputValues.year
        onChange = controller.oldBrowsersYearTextFieldOnChange
      }
    }
  }

  Box {
    if (controller.detectInputDateSupport()) {
      renderWithInputTypeDateSupport()
    } else {
      renderWithoutInputTypeDateSupport()
    }
  }
}
