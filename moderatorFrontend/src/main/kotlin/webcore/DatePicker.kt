package webcore

import com.studo.campusqr.common.utils.LocalizedString
import kotlinext.js.js
import kotlinx.browser.document
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import util.get
import webcore.extensions.emptyToNull
import webcore.extensions.inputValue
import webcore.extensions.setFullYear
import webcore.extensions.toInputTypeDateValueString
import webcore.materialUI.textField
import webcore.materialUI.withStyles
import kotlin.js.Date

interface DatePickerProps : RProps {
  var classes: DatePickerClasses
  var date: Date
  var onChange: (date: Date, isValid: Boolean) -> Unit
  var disabled: Boolean
  var error: Boolean
  var min: Date?
  var max: Date?
}

interface DatePickerState : RState {
  var dateTimeInputValue: String
  var oldBrowsersInputValues: DatePicker.DateInputValues
  var fieldError: Boolean
}

class DatePicker(props: DatePickerProps) : RComponent<DatePickerProps, DatePickerState>(props) {

  class DateInputValues(var year: String, var month: String, var day: String)

  override fun DatePickerState.init(props: DatePickerProps) {
    val year = props.date.getFullYear()
    val month = props.date.getMonth() + 1
    val day = props.date.getDate()
    dateTimeInputValue = props.date.toInputTypeDateValueString()
    oldBrowsersInputValues = DateInputValues(year.toString(), month.toString(), day.toString())
    fieldError = false
  }

  override fun componentDidUpdate(prevProps: DatePickerProps, prevState: DatePickerState, snapshot: Any) {
    if (prevProps.date != props.date) {
      setState { init(props) }
    }
  }

  private fun daysInMonth(month: Int, year: Int): Int {
    return Date(year, month, 0).getDate()
  }

  private fun detectInputDateSupport(): Boolean {
    val input = document.createElement("input") as HTMLInputElement
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
    props.onChange(date, isValid)
  }

  private fun RBuilder.renderWithInputTypeDateSupport() {
    textField {
      attrs.type = InputType.date.toString()
      attrs.value = state.dateTimeInputValue
      attrs.inputProps = js {
        props.min?.let { minProp ->
          min = minProp.toInputTypeDateValueString()
        }
        props.max?.let { maxProp ->
          max = maxProp.toInputTypeDateValueString()
        }
      }
      attrs.disabled = props.disabled
      attrs.error = props.error || state.fieldError
      attrs.onChange = { event: Event ->
        event.inputValue.emptyToNull()?.let { value ->
          setState {
            dateTimeInputValue = value
          }
          val inputElement = event.target as HTMLInputElement
          val inputDateTimestamp = inputElement.valueAsNumber
          val date = Date(inputDateTimestamp)

          val isValid: Boolean = !inputDateTimestamp.isNaN() && date.getFullYear() > 1000 && date.getFullYear() < 9999
          props.onChange(date, isValid)
          setState { fieldError = !isValid }
        }
      }
    }
  }

  private fun RBuilder.renderWithoutInputTypeDateSupport() {
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
    div(props.classes.flex) {
      textField {
        attrs.disabled = props.disabled
        attrs.error = props.error || state.fieldError
        attrs.style = js { flex = 1 }
        attrs.classes = js {
          root = props.classes.textFieldLabel
        }
        attrs.type = InputType.number.toString()
        attrs.placeholder = dayString
        attrs.label = dayString
        attrs.inputProps = js {
          min = 1
          max = 31
        }
        attrs.value = state.oldBrowsersInputValues.day
        attrs.onChange = { event: Event ->
          val value = event.inputValue
          setState { oldBrowsersInputValues.day = value }
          tryParsingInputFields(value, state.oldBrowsersInputValues.month, state.oldBrowsersInputValues.year)
        }
      }
      textField {
        attrs.disabled = props.disabled
        attrs.error = props.error || state.fieldError
        attrs.style = js { flex = 2 }
        attrs.classes = js {
          root = props.classes.textFieldLabel
        }
        attrs.type = InputType.number.toString()
        attrs.placeholder = monthString
        attrs.label = monthString
        attrs.inputProps = js {
          min = 1
          max = 12
        }
        attrs.value = state.oldBrowsersInputValues.month
        attrs.onChange = { event: Event ->
          val value = event.inputValue
          setState { oldBrowsersInputValues.month = value }
          tryParsingInputFields(state.oldBrowsersInputValues.day, value, state.oldBrowsersInputValues.year)
        }
      }
      textField {
        attrs.disabled = props.disabled
        attrs.error = props.error || state.fieldError
        attrs.style = js { flex = 2 }
        attrs.classes = js {
          root = props.classes.textFieldLabel
        }
        attrs.type = InputType.number.toString()
        attrs.placeholder = yearString
        attrs.label = yearString
        attrs.value = state.oldBrowsersInputValues.year
        attrs.onChange = { event: Event ->
          val value = event.inputValue
          setState { oldBrowsersInputValues.year = value }
          tryParsingInputFields(state.oldBrowsersInputValues.day, state.oldBrowsersInputValues.month, value)
        }
      }
    }
  }

  override fun RBuilder.render() {
    div {
      if (detectInputDateSupport()) {
        renderWithInputTypeDateSupport()
      } else {
        renderWithoutInputTypeDateSupport()
      }
    }
  }
}

interface DatePickerClasses {
  var flex: String
  var textFieldLabel: String
}

private val styles = { theme: dynamic ->
  js {
    flex = js {
      display = "flex"
      width = "100%"
    }
    textFieldLabel = js {
      paddingTop = theme.spacing(2)
    }
  }
}

private val styled = withStyles<DatePickerProps, DatePicker>(styles)

fun RBuilder.datePicker(
  date: Date,
  onChange: (date: Date, isValid: Boolean) -> Unit,
  disabled: Boolean = false,
  error: Boolean = false,
  min: Date? = null,
  max: Date? = null,
) = styled {
  attrs.date = date
  attrs.onChange = onChange
  attrs.disabled = disabled
  attrs.error = error
  attrs.min = min
  attrs.max = max
}
