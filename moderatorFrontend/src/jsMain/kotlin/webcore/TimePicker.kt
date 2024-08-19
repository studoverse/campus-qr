package webcore

import com.studo.campusqr.common.utils.LocalizedString
import web.cssom.*
import js.objects.jso
import mui.material.Box
import mui.material.FormControlVariant
import mui.material.TextField
import mui.system.sx
import web.html.HTMLElement
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.option
import util.get
import web.dom.document
import web.html.HTML
import webcore.extensions.*
import kotlin.js.Date

class TimePickerConfig(
  var time: Date,
  var onChange: (date: Date) -> Unit,
  var disabled: Boolean = false,
  var error: Boolean = false,
  var min: Date? = null,
  var max: Date? = null,
  var stepMinutes: Int = 1,
  var fullWidth: Boolean = false,
  var label: String? = null,
  var helperText: String? = null,
  var variant: FormControlVariant = FormControlVariant.outlined,
)

external interface TimePickerProps : Props {
  var config: TimePickerConfig
}

external interface TimePickerState : State {
  var oldBrowsersInputValues: TimePicker.TimeInputValues
}

class TimePicker(props: TimePickerProps) : RComponent<TimePickerProps, TimePickerState>(props) {

  private val stepListId = "timestep-list${hashCode()}"

  class TimeInputValues(var hour: String, var minute: String)

  override fun TimePickerState.init(props: TimePickerProps) {
    val timeValue = props.config.time.toInputTypeTimeValueString()
    oldBrowsersInputValues = TimeInputValues(timeValue.split(":")[0], timeValue.split(":")[1])
  }

  override fun componentDidUpdate(prevProps: TimePickerProps, prevState: TimePickerState, snapshot: Any) {
    if (prevProps.config.time != props.config.time) {
      setState { init(props) }
    }
  }

  private fun detectInputTimeSupport(): Boolean {
    val input = document.createElement(HTML.input)
    input.setAttribute("type", "time")
    val invalidDateValue = "not-a-time"
    input.setAttribute("value", invalidDateValue)
    return input.value != invalidDateValue
  }

  private fun tryParsingInputFields(hourInputValue: String, minuteInputValue: String) {
    try {
      val hour = hourInputValue.toInt()
      val minute = minuteInputValue.toInt()
      if (hour > 23 || hour < 0 || minute < 0 || minute > 59) {
        // Don't allow invalid input
        return
      }
      val date = Date().with(hour = hour, minute = minute, second = 0, millisecond = 0)
      props.config.onChange(date)
    } catch (e: Exception) {
    }
  }

  private fun ChildrenBuilder.renderWithInputTypeTimeSupport() {
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
        type = web.html.InputType.time
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

  private fun ChildrenBuilder.renderWithoutInputTypeTimeSupport() {
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
        type = web.html.InputType.number
        placeholder = hourString
        label = hourString.toReactNode()
        inputProps = jso {
          min = 0
          max = 23
        }
        value = state.oldBrowsersInputValues.hour
        this.onChange = { event ->
          val value: String = event.target.value
          tryParsingInputFields(value, state.oldBrowsersInputValues.minute)
        }
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
        type = web.html.InputType.number
        placeholder = minuteString
        label = minuteString.toReactNode()
        inputProps = jso {
          min = 0
          max = 59
        }
        value = state.oldBrowsersInputValues.minute
        onChange = { event ->
          val value: String = event.target.value
          tryParsingInputFields(state.oldBrowsersInputValues.hour, value)
        }
      }
    }
  }

  override fun ChildrenBuilder.render() {
    if (detectInputTimeSupport()) {
      renderWithInputTypeTimeSupport()
    } else {
      renderWithoutInputTypeTimeSupport()
    }
  }
}

fun ChildrenBuilder.timePicker(config: TimePickerConfig) {
  TimePicker::class.react {
    this.config = config
  }
}