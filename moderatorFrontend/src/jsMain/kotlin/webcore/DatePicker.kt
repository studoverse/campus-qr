package webcore

import com.studo.campusqr.common.utils.LocalizedString
import js.lazy.Lazy
import web.cssom.*
import js.objects.jso
import mui.material.Box
import mui.material.FormControlVariant
import mui.material.TextField
import mui.system.sx
import react.*
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

private class DateInputValues(var year: String, var month: String, var day: String)

//@Lazy
val DatePickerFc = FcWithCoroutineScope<DatePickerProps> { props, launch ->
  val year = props.config.date.getFullYear()
  val month = props.config.date.getMonth() + 1
  val day = props.config.date.getDate()

  var dateTimeInputValue: String by useState(props.config.date.toInputTypeDateValueString())
  var oldBrowsersInputValues: DateInputValues by useState(DateInputValues(year.toString(), month.toString(), day.toString()))
  var fieldError: Boolean by useState(false)

  useEffect(props.config.date) {
    dateTimeInputValue = props.config.date.toInputTypeDateValueString()
    oldBrowsersInputValues = DateInputValues(year.toString(), month.toString(), day.toString())
    fieldError = false
  }

  fun daysInMonth(month: Int, year: Int): Int {
    return Date(year, month, 0).getDate()
  }

  fun detectInputDateSupport(): Boolean {
    val input = document.createElement(HTML.input)
    input.setAttribute("type", "date")
    val invalidDateValue = "not-a-date"
    input.setAttribute("value", invalidDateValue)
    return input.value != invalidDateValue
  }

  fun tryParsingInputFields(dayInputValue: String, monthInputValue: String, yearInputValue: String) {
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

    fieldError = !isValid
    val date = Date().setFullYear(year, month, day)
    props.config.onChange(date, isValid)
  }

  fun ChildrenBuilder.renderWithInputTypeDateSupport() {
    TextField {
      type = web.html.InputType.date
      value = dateTimeInputValue
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
      error = props.config.error || fieldError

      onChange = { event ->
        event.target.value.emptyToNull()?.let { value ->
          dateTimeInputValue = value
          val inputElement = event.target
          val inputDateTimestamp = inputElement.valueAsNumber
          val date = Date(inputDateTimestamp)

          val isValid: Boolean = !inputDateTimestamp.isNaN() && date.getFullYear() > 1000 && date.getFullYear() < 9999
          props.config.onChange(date, isValid)
          fieldError = !isValid
        }
      }
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
        error = props.config.error || fieldError
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
        value = oldBrowsersInputValues.day
        onChange = { event ->
          val value = event.target.value
          oldBrowsersInputValues.day = value
          tryParsingInputFields(value, oldBrowsersInputValues.month, oldBrowsersInputValues.year)
        }
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error || fieldError
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
        value = oldBrowsersInputValues.month
        onChange = { event ->
          val value = event.target.value
          oldBrowsersInputValues.month = value
          tryParsingInputFields(oldBrowsersInputValues.day, value, oldBrowsersInputValues.year)
        }
      }
      TextField {
        fullWidth = props.config.fullWidth
        variant = props.config.variant
        disabled = props.config.disabled
        error = props.config.error || fieldError
        sx {
          flex = number(2.0)
          paddingTop = 16.px
        }
        type = web.html.InputType.number
        placeholder = yearString
        label = yearString.toReactNode()
        value = oldBrowsersInputValues.year
        onChange = { event ->
          val value = event.target.value
          oldBrowsersInputValues.year = value
          tryParsingInputFields(oldBrowsersInputValues.day, oldBrowsersInputValues.month, value)
        }
      }
    }
  }

  Box {
    if (detectInputDateSupport()) {
      renderWithInputTypeDateSupport()
    } else {
      renderWithoutInputTypeDateSupport()
    }
  }
}
