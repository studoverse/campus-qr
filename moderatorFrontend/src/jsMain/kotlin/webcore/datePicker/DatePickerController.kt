package webcore.datePicker

import react.useEffect
import react.useState
import web.dom.document
import web.html.HTMLInputElement
import webcore.Launch
import webcore.TextFieldOnChange
import webcore.datePicker.DatePickerConfig.DateInputValues
import webcore.extensions.emptyToNull
import webcore.extensions.setFullYear
import webcore.extensions.toInputTypeDateValueString
import kotlin.js.Date

data class DatePickerController(
  val oldBrowsersInputValues: DateInputValues,
  val dateTimeInputValue: String,
  val fieldError: Boolean,
  val daysInMonth: (month: Int, year: Int) -> Int,
  val detectInputDateSupport: () -> Boolean,
  val tryParsingInputFields: (dayInputValue: String, monthInputValue: String, yearInputValue: String) -> Unit,
  val newBrowsersDateTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersDayTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersMonthTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersYearTextFieldOnChange: TextFieldOnChange,
) {
  companion object {
    fun use(launch: Launch, props: DatePickerProps): DatePickerController {
      val year = props.config.date.getFullYear()
      val month = props.config.date.getMonth() + 1
      val day = props.config.date.getDate()

      var oldBrowsersInputValues: DateInputValues by useState(DateInputValues(year.toString(), month.toString(), day.toString()))
      var dateTimeInputValue: String by useState(props.config.date.toInputTypeDateValueString())
      var fieldError: Boolean by useState(false)

      fun daysInMonth(month: Int, year: Int): Int {
        return Date(year, month + 1, 0).getDate() // Add 1 to the month since day = 0 gets last day of the month before
      }

      fun detectInputDateSupport(): Boolean {
        val input = document.createElement("input") as HTMLInputElement
        input.setAttribute("type", "date")
        val invalidDateValue = "not-a-date"
        input.setAttribute("value", invalidDateValue)
        return input.value != invalidDateValue
      }

      fun tryParsingInputFields(dayInputValue: String, monthInputValue: String, yearInputValue: String) {
        val now = Date()
        var day: Int = now.getDate()
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

      val datePickerWithInputTypeDateSupportOnChange: TextFieldOnChange = { event ->
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

      val oldBrowsersDayTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        oldBrowsersInputValues = DateInputValues(oldBrowsersInputValues.year, oldBrowsersInputValues.month, value)
        tryParsingInputFields(value, oldBrowsersInputValues.month, oldBrowsersInputValues.year)
      }

      val oldBrowsersMonthTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        oldBrowsersInputValues = DateInputValues(oldBrowsersInputValues.year, value, oldBrowsersInputValues.day)
        tryParsingInputFields(oldBrowsersInputValues.day, value, oldBrowsersInputValues.year)
      }

      val oldBrowsersYearTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        oldBrowsersInputValues = DateInputValues(value, oldBrowsersInputValues.month, oldBrowsersInputValues.day)
        tryParsingInputFields(oldBrowsersInputValues.day, oldBrowsersInputValues.month, value)
      }

      useEffect(props.config.date) {
        dateTimeInputValue = props.config.date.toInputTypeDateValueString()
        oldBrowsersInputValues = DateInputValues(year.toString(), month.toString(), day.toString())
        fieldError = false
      }

      return DatePickerController(
        oldBrowsersInputValues = oldBrowsersInputValues,
        dateTimeInputValue = dateTimeInputValue,
        fieldError = fieldError,
        daysInMonth = ::daysInMonth,
        detectInputDateSupport = ::detectInputDateSupport,
        tryParsingInputFields = ::tryParsingInputFields,
        newBrowsersDateTextFieldOnChange = datePickerWithInputTypeDateSupportOnChange,
        oldBrowsersDayTextFieldOnChange = oldBrowsersDayTextFieldOnChange,
        oldBrowsersMonthTextFieldOnChange = oldBrowsersMonthTextFieldOnChange,
        oldBrowsersYearTextFieldOnChange = oldBrowsersYearTextFieldOnChange,
      )
    }
  }
}