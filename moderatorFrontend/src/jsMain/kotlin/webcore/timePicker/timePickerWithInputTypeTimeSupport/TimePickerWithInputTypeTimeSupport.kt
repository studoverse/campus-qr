package webcore.timePicker.timePickerWithInputTypeTimeSupport

import js.lazy.Lazy
import js.objects.unsafeJso
import mui.material.Box
import mui.material.TextField
import mui.system.sx
import react.Props
import react.dom.html.ReactHTML
import web.cssom.WhiteSpace
import web.cssom.pct
import web.dom.ElementId
import web.html.InputType
import web.html.time
import kotlin.js.Date
import webcore.FcWithCoroutineScope
import webcore.extensions.toInputTypeTimeValueString
import webcore.toReactNode
import webcore.onChange
import webcore.min
import webcore.max
import webcore.list
import webcore.extensions.startOfTheDay
import webcore.extensions.endOfTheDay
import webcore.extensions.with
import webcore.extensions.emptyToNull

external interface TimePickerWithInputTypeTimeSupportProps : Props {
  var config: TimePickerWithInputTypeTimeSupportConfig
}

@Lazy
val TimePickerWithInputTypeTimeSupport = FcWithCoroutineScope<TimePickerWithInputTypeTimeSupportProps> { props, launch ->
  val stepListId = "timestep-list${hashCode()}"
  Box {
    sx {
      width = 100.pct
    }
    TextField {
      variant = props.config.variant
      value = props.config.time.toInputTypeTimeValueString()
      inputProps = unsafeJso {
        props.config.min?.let { minProp ->
          min = minProp.toInputTypeTimeValueString()
        }
        props.config.max?.let { maxProp ->
          max = maxProp.toInputTypeTimeValueString()
        }
        list = stepListId
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
      error = props.config.error
      type = InputType.time
      onChange = { event ->
        event.target.value.emptyToNull()?.let { value ->
          val hourToMinute = value.split(":").map { it.removePrefix("0").toInt() }
          val hour = hourToMinute[0]
          val minute = hourToMinute[1]
          props.config.onChange(props.config.time.with(hour = hour, minute = minute))
        }
      }
    }
    if (props.config.stepMinutes != 1) { // Create time options list if step is not the default value of 1 minute
      ReactHTML.datalist {
        id = ElementId(stepListId)
        // Control variable to fill options list
        var timestampIterControl = Date().startOfTheDay().getTime()
        val endOfDay = Date(timestampIterControl).endOfTheDay().getTime()
        while (timestampIterControl <= endOfDay) {
          ReactHTML.option {
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