package webcore.timePicker

import com.studo.campusqr.common.utils.LocalizedString
import js.lazy.Lazy
import web.cssom.*
import js.objects.jso
import mui.material.Box
import mui.material.TextField
import mui.system.sx
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.option
import util.get
import web.html.InputType
import webcore.FcWithCoroutineScope
import webcore.extensions.*
import webcore.list
import webcore.max
import webcore.min
import webcore.onChange
import webcore.toReactNode
import kotlin.js.Date

external interface TimePickerProps : Props {
  var config: TimePickerConfig
}

@Lazy
val TimePicker = FcWithCoroutineScope<TimePickerProps> { props, launch ->
  val controller: TimePickerController = TimePickerController.useTimePickerController(config = props.config, launch = launch)

  val stepListId = useMemo(*emptyArray<Any>()) { "timestep-list${hashCode()}" }

  fun ChildrenBuilder.renderWithInputTypeTimeSupport() {
    Box {
      sx {
        width = 100.pct
      }
      TextField {
        value = props.config.time.toInputTypeTimeValueString()
        inputProps = jso {
          props.config.min?.let { minProp ->
            min = minProp.toInputTypeTimeValueString()
          }
          props.config.max?.let { maxProp ->
            max = maxProp.toInputTypeTimeValueString()
          }
          list = stepListId
        }
        label = (props.config.label ?: "").toReactNode()
        helperText = (props.config.helperText ?: "").toReactNode()
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error
        type = InputType.time
        onChange = controller.newBrowsersTimeTextFieldOnChange
      }
      if (props.config.stepMinutes != 1) { // Create time options list if step is not the default value of 1 minute
        ReactHTML.datalist {
          id = stepListId
          // Control variable to fill options list
          var timestampIterControl = Date().startOfTheDay().getTime()
          val endOfDay = Date(timestampIterControl).endOfTheDay().getTime()
          while (timestampIterControl <= endOfDay) {
            option {
              val currentDate = Date(timestampIterControl)
              value = currentDate.getHours().toString().padStart(2, '0') + ":" +
                  currentDate.getMinutes().toString().padStart(2, '0') // HH:mm
            }
            timestampIterControl += (props.config.stepMinutes * 60 * 1000) // Minutes to milliseconds
          }
        }
      }
    }
  }

  fun ChildrenBuilder.renderWithoutInputTypeTimeSupport() {
    val hourString = LocalizedString(
      "Hour",
      "Stunde"
    ).get()
    val minuteString = LocalizedString(
      "Minute",
      "Minute"
    ).get()
    Box.create {
      sx {
        width = 100.pct
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error
        sx {
          flex = number(1.0)
          paddingTop = 16.px
        }
        type = InputType.number
        placeholder = hourString
        label = hourString.toReactNode()
        inputProps = jso {
          min = 0
          max = 23
        }
        value = controller.oldBrowsersInputValues.hour
        this.onChange = controller.oldBrowsersHourTextFieldOnChange
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error
        sx {
          flex = number(1.0)
          paddingTop = 16.px
        }
        type = InputType.number
        placeholder = minuteString
        label = minuteString.toReactNode()
        inputProps = jso {
          min = 0
          max = 59
        }
        value = controller.oldBrowsersInputValues.minute
        onChange = controller.oldBrowsersMinuteTextFieldOnChange
      }
    }
  }

  if (controller.detectInputTimeSupport()) {
    renderWithInputTypeTimeSupport()
  } else {
    renderWithoutInputTypeTimeSupport()
  }
}
