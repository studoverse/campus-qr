package com.moshbit.backend.core.ui.timePicker

import react.useEffect
import react.useMemo
import react.useState
import web.dom.document
import web.html.HTMLInputElement
import webcore.timePicker.TimePickerConfig
import webcore.extensions.with
import webcore.Launch
import webcore.extensions.toInputTypeTimeValueString
import webcore.timePicker.TimePickerProps
import kotlin.js.Date

data class TimePickerController(
  val oldBrowsersInputValues: TimePickerConfig.Companion.TimeInputValues,
  val tryParsingInputFields: (hourInputValue: String, minuteInputValue: String) -> Unit,
  val detectInputTimeSupport: () -> Boolean,
) {
  companion object {
    fun use(launch: Launch, props: TimePickerProps): TimePickerController {
      val timeValue = useMemo(props.config.time) { props.config.time.toInputTypeTimeValueString() }
      var oldBrowsersInputValues: TimePickerConfig.Companion.TimeInputValues by useState(
        TimePickerConfig.Companion.TimeInputValues(timeValue.split(":")[0], timeValue.split(":")[1])
      )

      fun detectInputTimeSupport(): Boolean {
        val input = document.createElement("input") as HTMLInputElement
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
          props.config.onChange(date)
        } catch (e: Exception) {
        }
      }

      useEffect(props.config.time) {
        oldBrowsersInputValues = TimePickerConfig.Companion.TimeInputValues(timeValue.split(":")[0], timeValue.split(":")[1])
      }

      return TimePickerController(
        oldBrowsersInputValues = oldBrowsersInputValues,
        detectInputTimeSupport = ::detectInputTimeSupport,
        tryParsingInputFields = ::tryParsingInputFields
      )
    }
  }
}