package webcore.timePicker

import react.useEffect
import react.useState
import web.dom.document
import web.html.HTML
import webcore.Launch
import webcore.TextFieldOnChange
import webcore.extensions.emptyToNull
import webcore.extensions.toInputTypeTimeValueString
import webcore.extensions.with
import kotlin.js.Date

data class TimePickerController(
  val oldBrowsersInputValues: TimeInputValues,
  val detectInputTimeSupport: () -> Boolean,
  val oldBrowsersHourTextFieldOnChange: TextFieldOnChange,
  val oldBrowsersMinuteTextFieldOnChange: TextFieldOnChange,
  val newBrowsersTimeTextFieldOnChange: TextFieldOnChange,
) {
  companion object {
    fun use(config: TimePickerConfig, launch: Launch): TimePickerController {
      val timeValue = config.time.toInputTypeTimeValueString()
      var oldBrowsersInputValues: TimeInputValues by useState(TimeInputValues(timeValue.split(":")[0], timeValue.split(":")[1]))

      fun detectInputTimeSupport(): Boolean {
        val input = document.createElement(HTML.input)
        input.setAttribute("type", "time")
        val invalidDateValue = "not-a-time"
        input.setAttribute("value", invalidDateValue)
        return input.value != invalidDateValue
      }

      fun tryParsingInputFields(hourInputValue: String, minuteInputValue: String) {
        try {
          val hour = hourInputValue.toInt()
          val minute = minuteInputValue.toInt()
          if (hour > 23 || hour < 0 || minute < 0 || minute > 59) {
            // Don't allow invalid input
            return
          }
          val date = Date().with(hour = hour, minute = minute, second = 0, millisecond = 0)
          config.onChange(date)
        } catch (e: Exception) {
        }
      }

      val oldBrowsersHourTextFieldOnChange: TextFieldOnChange = { event ->
        val value: String = event.target.value
        tryParsingInputFields(value, oldBrowsersInputValues.minute)
      }

      val oldBrowsersMinuteTextFieldOnChange: TextFieldOnChange = { event ->
        val value: String = event.target.value
        tryParsingInputFields(oldBrowsersInputValues.hour, value)
      }

      val newBrowsersTimeTextFieldOnChange: TextFieldOnChange = { event ->
        event.target.value.emptyToNull()?.let { value ->
          val hourToMinute = value.split(":").map { it.removePrefix("0").toInt() }
          val hour = hourToMinute[0]
          val minute = hourToMinute[1]
          config.onChange(config.time.with(hour = hour, minute = minute))
        }
      }

      useEffect(config.time) {
        oldBrowsersInputValues = TimeInputValues(timeValue.split(":")[0], timeValue.split(":")[1])
      }

      return TimePickerController(
        oldBrowsersInputValues = oldBrowsersInputValues,
        detectInputTimeSupport = ::detectInputTimeSupport,
        oldBrowsersHourTextFieldOnChange = oldBrowsersHourTextFieldOnChange,
        oldBrowsersMinuteTextFieldOnChange = oldBrowsersMinuteTextFieldOnChange,
        newBrowsersTimeTextFieldOnChange = newBrowsersTimeTextFieldOnChange,
      )
    }

    class TimeInputValues(var hour: String, var minute: String)
  }
}