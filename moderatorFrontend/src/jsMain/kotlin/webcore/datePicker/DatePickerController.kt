package webcore.datePicker

import react.useEffect
import react.useState
import web.dom.document
import web.html.HTML
import webcore.Launch
import webcore.TextFieldOnChange
import webcore.extensions.emptyToNull
import webcore.extensions.setFullYear
import webcore.extensions.toInputTypeDateValueString
import kotlin.js.Date

data class DatePickerController(
  val oldBrowsersInputValues: DateInputValues,
  val dateTimeInputValue: String,
  val fieldError: Boolean,
  val detectInputDateSupport: () -> Boolean,
  val oldBrowsersDayTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersMonthTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersYearTextFieldOnChange: TextFieldOnChange,
  val newBrowsersDateTextFieldOnChange: TextFieldOnChange,
) {
  companion object {
    fun useDatePickerController(config: DatePickerConfig, launch: Launch): DatePickerController {
      val year = config.date.getFullYear()
      val month = config.date.getMonth() + 1
      val day = config.date.getDate()

      var dateTimeInputValue: String by useState(config.date.toInputTypeDateValueString())
      var oldBrowsersInputValues: DateInputValues by useState(DateInputValues(year.toString(), month.toString(), day.toString()))
      var fieldError: Boolean by useState(false)

      useEffect(config.date) {
        dateTimeInputValue = config.date.toInputTypeDateValueString()
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
        config.onChange(date, isValid)
      }

      val oldBrowsersDayTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        oldBrowsersInputValues.day = value
        tryParsingInputFields(value, oldBrowsersInputValues.month, oldBrowsersInputValues.year)
      }

      val oldBrowsersMonthTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        oldBrowsersInputValues.month = value
        tryParsingInputFields(oldBrowsersInputValues.day, value, oldBrowsersInputValues.year)
      }

      val oldBrowsersYearTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        oldBrowsersInputValues.year = value
        tryParsingInputFields(oldBrowsersInputValues.day, oldBrowsersInputValues.month, value)
      }

      val newBrowsersDateTextFieldOnChange: TextFieldOnChange = { event ->
        event.target.value.emptyToNull()?.let { value ->
          dateTimeInputValue = value
          val inputElement = event.target
          val inputDateTimestamp = inputElement.valueAsNumber
          val date = Date(inputDateTimestamp)

          val isValid: Boolean = !inputDateTimestamp.isNaN() && date.getFullYear() > 1000 && date.getFullYear() < 9999
          config.onChange(date, isValid)
          fieldError = !isValid
        }
      }

      return DatePickerController(
        oldBrowsersInputValues = oldBrowsersInputValues,
        dateTimeInputValue = dateTimeInputValue,
        fieldError = fieldError,
        detectInputDateSupport = ::detectInputDateSupport,
        oldBrowsersDayTextFieldOnChange = oldBrowsersDayTextFieldOnChange,
        oldBrowsersMonthTextFieldOnChange = oldBrowsersMonthTextFieldOnChange,
        oldBrowsersYearTextFieldOnChange = oldBrowsersYearTextFieldOnChange,
        newBrowsersDateTextFieldOnChange = newBrowsersDateTextFieldOnChange,
      )
    }

    class DateInputValues(var year: String, var month: String, var day: String)
  }
}