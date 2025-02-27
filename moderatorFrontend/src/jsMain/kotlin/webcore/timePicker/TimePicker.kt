package webcore.timePicker

import com.moshbit.backend.core.ui.timePicker.TimePickerController
import webcore.timePicker.timePickerWithInputTypeTimeSupport.TimePickerWithInputTypeTimeSupport
import webcore.timePicker.timePickerWithInputTypeTimeSupport.TimePickerWithInputTypeTimeSupportConfig
import webcore.timePicker.timePickerWithoutInputTypeTimeSupport.TimePickerWithoutInputTypeTimeSupport
import webcore.timePicker.timePickerWithoutInputTypeTimeSupport.TimePickerWithoutInputTypeTimeSupportConfig
import js.lazy.Lazy
import react.*
import webcore.FcWithCoroutineScope

external interface TimePickerProps : Props {
  var config: TimePickerConfig
}

@Lazy
val TimePicker = FcWithCoroutineScope<TimePickerProps> { props, launch ->
  val controller = TimePickerController.use(launch = launch, props)

  if (controller.detectInputTimeSupport()) {
    TimePickerWithInputTypeTimeSupport {
      config = TimePickerWithInputTypeTimeSupportConfig(
        time = props.config.time,
        onChange = props.config.onChange,
        min = props.config.min,
        max = props.config.max,
        stepMinutes = props.config.stepMinutes,
        disabled = props.config.disabled,
        error = props.config.error,
        fullWidth = props.config.fullWidth,
        label = props.config.label,
        helperText = props.config.helperText,
        variant = props.config.variant,
      )
    }
  } else {
    TimePickerWithoutInputTypeTimeSupport {
      config = TimePickerWithoutInputTypeTimeSupportConfig(
        oldBrowsersInputValues = controller.oldBrowsersInputValues,
        disabled = props.config.disabled,
        error = props.config.error,
        fullWidth = props.config.fullWidth,
        variant = props.config.variant,
        tryParsingInputFields = controller.tryParsingInputFields,
      )
    }
  }
}
