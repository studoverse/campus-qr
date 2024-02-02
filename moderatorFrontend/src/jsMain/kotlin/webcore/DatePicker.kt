package webcore

import com.studo.campusqr.common.utils.LocalizedString
import web.cssom.*
import js.objects.jso
import mui.material.Box
import mui.material.FormControlVariant
import mui.material.TextField
import mui.system.sx
import web.html.HTMLElement
import web.html.HTMLInputElement
import react.*
import react.dom.events.ChangeEvent
import react.dom.onChange
import util.get
import web.dom.document
import web.html.HTML
import webcore.extensions.emptyToNull
import webcore.extensions.setFullYear
import webcore.extensions.toInputTypeDateValueString
import kotlin.js.Date

class DatePickerConfig(
  var date: Date,
  var onChange: (date: Date, isValid: Boolean) -> Unit,
  var disabled: Boolean = false,
  var error: Boolean = false,
  var min: Date? = null,
  var max: Date? = null,
  var fullWidth: Boolean = false,
  var label: String? = null,
  var helperText: String? = null,
  var variant: FormControlVariant = FormControlVariant.outlined,
)

external interface DatePickerProps : Props {
  var config: DatePickerConfig
}

external interface DatePickerState : State {
  var dateTimeInputValue: String
  var oldBrowsersInputValues: DatePicker.DateInputValues
  var fieldError: Boolean
}

@Suppress("UPPER_BOUND_VIOLATED")
class DatePicker(props: DatePickerProps) : RComponent<DatePickerProps, DatePickerState>(props) {

  class DateInputValues(var year: String, var month: String, var day: String)

  override fun DatePickerState.init(props: DatePickerProps) {
    val year = props.config.date.getFullYear()
    val month = props.config.date.getMonth() + 1
    val day = props.config.date.getDate()
    dateTimeInputValue = props.config.date.toInputTypeDateValueString()
    oldBrowsersInputValues = DateInputValues(year.toString(), month.toString(), day.toString())
    fieldError = false
  }

  override fun componentDidUpdate(prevProps: DatePickerProps, prevState: DatePickerState, snapshot: Any) {
    if (prevProps.config.date != props.config.date) {
      setState { init(props) }
    }
  }

  private fun daysInMonth(month: Int, year: Int): Int {
    return Date(year, month, 0).getDate()
  }

  private fun detectInputDateSupport(): Boolean {
    val input = document.createElement(HTML.input)
    input.setAttribute("type", "date")
    val invalidDateValue = "not-a-date"
    input.setAttribute("value", invalidDateValue)
    return input.value != invalidDateValue
  }

  private fun tryParsingInputFields(dayInputValue: String, monthInputValue: String, yearInputValue: String) {
    val now = Date()
    var day: Int = now.getDay()
    var month: Int = now.getMonth()
    var year: Int = now.getFullYear()
    var isValid: Boolean
    try {
      day = dayInputValue.toInt()
      month = monthInputValue.toInt() - 1 // 0 - based month [0-11]
      year = yearInputValue.toInt()

      isValid = !(day > 31 || day < 1 || month < 0 || month > 11 || year !in 1000..9999 || daysInMonth(month, year) < day)
    } catch (e: Throwable) {
      isValid = false
    }

    setState {
      fieldError = !isValid
    }
    val date = Date().setFullYear(year, month, day)
    props.config.onChange(date, isValid)
  }

  private fun ChildrenBuilder.renderWithInputTypeDateSupport() {
    TextField {
      type = web.html.InputType.date
      value = state.dateTimeInputValue
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
      error = props.config.error || state.fieldError

      onChange = { event ->
        @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        event as ChangeEvent<HTMLElement>
        event.target.value.emptyToNull()?.let { value ->
          setState {
            dateTimeInputValue = value
          }
          val inputElement = event.target as HTMLInputElement
          val inputDateTimestamp = inputElement.valueAsNumber
          val date = Date(inputDateTimestamp)

          val isValid: Boolean = !inputDateTimestamp.isNaN() && date.getFullYear() > 1000 && date.getFullYear() < 9999
          props.config.onChange(date, isValid)
          setState { fieldError = !isValid }
        }
      }
    }
  }

  private fun ChildrenBuilder.renderWithoutInputTypeDateSupport() {
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
        error = props.config.error || state.fieldError
        sx {
          flex = number(1.0)
          paddingTop = 16.px
        }
        type = web.html.InputType.number
        placeholder = dayString
        label = dayString.toReactNode()
        inputProps = jso {
          min = 1
          max = 31
        }
        value = state.oldBrowsersInputValues.day
        onChange = { event ->
          @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
          event as ChangeEvent<HTMLElement>
          val value = event.target.value
          setState { oldBrowsersInputValues.day = value }
          tryParsingInputFields(value, state.oldBrowsersInputValues.month, state.oldBrowsersInputValues.year)
        }
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error || state.fieldError
        sx {
          flex = number(2.0)
          paddingTop = 16.px
        }
        type = web.html.InputType.number
        placeholder = monthString
        label = monthString.toReactNode()
        inputProps = jso {
          min = 1
          max = 12
        }
        value = state.oldBrowsersInputValues.month
        onChange = { event ->
          @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
          event as ChangeEvent<HTMLElement>
          val value = event.target.value
          setState { oldBrowsersInputValues.month = value }
          tryParsingInputFields(state.oldBrowsersInputValues.day, value, state.oldBrowsersInputValues.year)
        }
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error || state.fieldError
        sx {
          flex = number(2.0)
          paddingTop = 16.px
        }
        type = web.html.InputType.number
        placeholder = yearString
        label = yearString.toReactNode()
        value = state.oldBrowsersInputValues.year
        onChange = { event ->
          @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
          event as ChangeEvent<HTMLElement>
          val value = event.target.value
          setState { oldBrowsersInputValues.year = value }
          tryParsingInputFields(state.oldBrowsersInputValues.day, state.oldBrowsersInputValues.month, value)
        }
      }
    }
  }

  override fun ChildrenBuilder.render() {
    Box {
      if (detectInputDateSupport()) {
        renderWithInputTypeDateSupport()
      } else {
        renderWithoutInputTypeDateSupport()
      }
    }
  }
}

fun ChildrenBuilder.datePicker(config: DatePickerConfig) {
  DatePicker::class.react {
    this.config = config
  }
}
