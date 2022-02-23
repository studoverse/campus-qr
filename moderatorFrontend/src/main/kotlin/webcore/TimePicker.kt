package webcore

import com.studo.campusqr.common.utils.LocalizedString
import kotlinext.js.js
import kotlinx.browser.document
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.datalist
import react.dom.div
import react.dom.option
import util.get
import webcore.extensions.*
import webcore.materialUI.TextFieldVariant
import webcore.materialUI.textField
import webcore.materialUI.withStyles
import kotlin.js.Date

external interface TimePickerProps : RProps {
  var classes: TimePickerClasses
  var time: Date
  var onChange: (date: Date) -> Unit
  var disabled: Boolean
  var error: Boolean
  var min: Date?
  var max: Date?
  var stepMinutes: Int
  var fullWidth: Boolean
  var label: String?
  var helperText: String?
  var variant: TextFieldVariant
}

external interface TimePickerState : RState {
  var oldBrowsersInputValues: TimePicker.TimeInputValues
}

class TimePicker(props: TimePickerProps) : RComponent<TimePickerProps, TimePickerState>(props) {

  private val stepListId = "timestep-list${hashCode()}"

  class TimeInputValues(var hour: String, var minute: String)

  override fun TimePickerState.init(props: TimePickerProps) {
    val timeValue = props.time.toInputTypeTimeValueString()
    oldBrowsersInputValues = TimeInputValues(timeValue.split(":")[0], timeValue.split(":")[1])
  }

  override fun componentDidUpdate(prevProps: TimePickerProps, prevState: TimePickerState, snapshot: Any) {
    if (prevProps.time != props.time) {
      setState { init(props) }
    }
  }

  private fun detectInputTimeSupport(): Boolean {
    val input = document.createElement("input") as HTMLInputElement
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
      props.onChange(date)
    } catch (e: Exception) {
    }
  }

  private fun RBuilder.renderWithInputTypeTimeSupport() {
    div(props.classes.container) {
      textField {
        attrs.value = props.time.toInputTypeTimeValueString()
        attrs.inputProps = js {
          props.min?.let { minProp ->
            min = minProp.toInputTypeTimeValueString()
          }
          props.max?.let { maxProp ->
            max = maxProp.toInputTypeTimeValueString()
          }
          list = stepListId
        }
        attrs.label = props.label
        attrs.helperText = props.helperText
        attrs.fullWidth = props.fullWidth
        attrs.variant = props.variant.value
        attrs.disabled = props.disabled
        attrs.error = props.error
        attrs.type = InputType.time.toString()
        attrs.onChange = { event: Event ->
          event.inputValue.emptyToNull()?.let { value ->
            val hourToMinute = value.split(":").map { it.removePrefix("0").toInt() }
            val hour = hourToMinute[0]
            val minute = hourToMinute[1]
            props.onChange(props.time.with(hour = hour, minute = minute))
          }
        }
      }
      if (props.stepMinutes != 1) { // Create time options list if step is not the default value of 1 minute
        datalist {
          attrs["id"] = stepListId
          // Control variable to fill options list
          var timestampIterControl = Date().startOfTheDay().getTime()
          val endOfDay = Date(timestampIterControl).endOfTheDay().getTime()
          while (timestampIterControl <= endOfDay) {
            option {
              val currentDate = Date(timestampIterControl)
              attrs.value = currentDate.getHours().toString().padStart(2, '0') + ":" +
                  currentDate.getMinutes().toString().padStart(2, '0') // HH:mm
            }
            timestampIterControl += (props.stepMinutes * 60 * 1000) // Minutes to milliseconds
          }
        }
      }
    }
  }

  private fun RBuilder.renderWithoutInputTypeTimeSupport() {
    val hourString = LocalizedString(
      "Hour",
      "Stunde"
    ).get()
    val minuteString = LocalizedString(
      "Minute",
      "Minute"
    ).get()
    div(props.classes.container) {
      textField {
        attrs.fullWidth = props.fullWidth
        attrs.variant = props.variant.value
        attrs.disabled = props.disabled
        attrs.error = props.error
        attrs.style = js { flex = 1 }
        attrs.classes = js {
          root = props.classes.textFieldLabel
        }
        attrs.type = InputType.number.toString()
        attrs.placeholder = hourString
        attrs.label = hourString
        attrs.inputProps = js {
          min = 0
          max = 23
        }
        attrs.value = state.oldBrowsersInputValues.hour
        attrs.onChange = { event: Event ->
          val value = event.inputValue
          tryParsingInputFields(value, state.oldBrowsersInputValues.minute)
        }
      }
      textField {
        attrs.fullWidth = props.fullWidth
        attrs.variant = props.variant.value
        attrs.disabled = props.disabled
        attrs.error = props.error
        attrs.style = js { flex = 1 }
        attrs.classes = js {
          root = props.classes.textFieldLabel
        }
        attrs.type = InputType.number.toString()
        attrs.placeholder = minuteString
        attrs.label = minuteString
        attrs.inputProps = js {
          min = 0
          max = 59
        }
        attrs.value = state.oldBrowsersInputValues.minute
        attrs.onChange = { event: Event ->
          val value = event.inputValue
          tryParsingInputFields(state.oldBrowsersInputValues.hour, value)
        }
      }
    }
  }

  override fun RBuilder.render() {
    if (detectInputTimeSupport()) {
      renderWithInputTypeTimeSupport()
    } else {
      renderWithoutInputTypeTimeSupport()
    }
  }
}

interface TimePickerClasses {
  var container: String
  var textFieldLabel: String
}

private val TimePickerStyle = { theme: dynamic ->
  js {
    container = js {
      width = "100%"
    }
    textFieldLabel = js {
      paddingTop = theme.spacing(2)
    }
  }
}

private val styled = withStyles<TimePickerProps, TimePicker>(TimePickerStyle)

fun RBuilder.timePicker(
  time: Date,
  onChange: (date: Date) -> Unit,
  disabled: Boolean = false,
  error: Boolean = false,
  min: Date? = null,
  max: Date? = null,
  stepMinutes: Int = 1,
  fullWidth: Boolean = false,
  label: String? = null,
  helperText: String? = null,
  variant: TextFieldVariant = TextFieldVariant.STANDARD,
) = styled {
  attrs.time = time
  attrs.onChange = onChange
  attrs.disabled = disabled
  attrs.error = error
  attrs.min = min
  attrs.max = max
  attrs.stepMinutes = stepMinutes
  attrs.fullWidth = fullWidth
  attrs.label = label
  attrs.helperText = helperText
  attrs.variant = variant
}