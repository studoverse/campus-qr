package views.accessManagement.accessManagementDetails.timeSlotPicker

import app.GlobalCss
import app.appContextToInject
import csstype.PropertiesBuilder
import js.lazy.Lazy
import mui.icons.material.Add
import mui.icons.material.Close
import mui.material.Box
import mui.material.FormControlVariant
import mui.material.GridDirection
import mui.material.IconButton
import mui.material.Tooltip
import mui.material.Typography
import mui.system.sx
import react.Props
import react.Suspense
import react.dom.html.ReactHTML.span
import util.Strings
import util.get
import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import views.common.horizontalSpacer
import views.common.spacer
import web.cssom.AlignItems
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.Flex
import web.cssom.FlexDirection
import web.cssom.number
import web.cssom.pct
import web.cssom.px
import webcore.datePicker.DatePickerConfig
import webcore.datePicker.DatePicker
import webcore.FcWithCoroutineScope
import webcore.GridSize
import webcore.timePicker.TimePickerConfig
import webcore.timePicker.TimePicker
import webcore.extensions.addYears
import webcore.gridContainer
import webcore.gridItem
import webcore.toReactNode
import kotlin.js.Date

external interface TimeSlotPickerProps : Props {
  var config: TimeSlotPickerConfig
}

@Lazy
val TimeSlotPicker = FcWithCoroutineScope<TimeSlotPickerProps> { props, launch ->
  val appContext = react.use(appContextToInject)!!

  fun PropertiesBuilder.timeSlotRow() {
    display = Display.flex
    flexDirection = FlexDirection.row
  }

  fun PropertiesBuilder.timeSlotColumn() {
    flex = Flex(number(0.0), number(1.0), 50.pct)
  }

  val theme = appContext.theme
  val now = Date()
  val inThreeYears = now.addYears(3)
  Box {
    className = ClassName(GlobalCss.flex)
    Typography {
      +Strings.access_control_time_slots.get()
    }
    if (props.config.accessManagementDetailsType !is AccessManagementDetailsConfig.Details) {
      Tooltip {
        title = Strings.access_control_time_slot_add.get().toReactNode()
        IconButton {
          sx {
            padding = 0.px
            marginLeft = 8.px
          }
          Add()
          onClick = props.config.addTimeSlotOnClick
        }
      }
    }
  }
  spacer(12, key = "timeSlotsSpacer1")
  props.config.timeSlots.forEachIndexed { index, clientDateRange ->
    gridContainer(GridDirection.row, alignItems = AlignItems.center, spacing = 1, key = "gridContainer$index") {
      gridItem(GridSize(xs = 12, sm = true), key = "gridItem$index") {
        Box {
          sx {
            timeSlotRow()
          }
          Box {
            sx {
              timeSlotColumn()
            }
            Suspense {
              DatePicker {
                config = DatePickerConfig(
                  disabled = props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Details,
                  date = Date(clientDateRange.from),
                  label = Strings.access_control_from.get(),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Create) now else null,
                  max = inThreeYears,
                  onChange = { selectedDate, _ ->
                    props.config.timeSlotDateFromOnChange(
                      selectedDate,
                      clientDateRange,
                      now,
                      inThreeYears,
                    )
                  },
                )
              }
            }
          }
          horizontalSpacer(12, key = "timeSlotColumnFromSpacer${index}")
          Box {
            sx {
              timeSlotColumn()
            }
            Suspense {
              TimePicker {
                config = TimePickerConfig(
                  disabled = props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Details,
                  time = Date(clientDateRange.from),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Create) now else null,
                  onChange = { selectedTime ->
                    props.config.timeSlotTimeFromOnChange(
                      selectedTime,
                      clientDateRange,
                    )
                  },
                )
              }
            }
          }
        }
        spacer(16, key = "timeSlotRowSpacer${index}")
        Box {
          sx {
            timeSlotRow()
          }
          Box {
            sx {
              timeSlotColumn()
            }
            Suspense {
              DatePicker {
                config = DatePickerConfig(
                  disabled = props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Details,
                  date = Date(clientDateRange.to),
                  label = Strings.access_control_to.get(),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Create) now else null,
                  max = inThreeYears,
                  onChange = { selectedDate, _ ->
                    props.config.timeSlotDateToOnChange(
                      selectedDate,
                      clientDateRange,
                      now,
                      inThreeYears,
                    )
                  },
                )
              }
            }
          }
          horizontalSpacer(12, key = "timeSlotColumnToSpacer${index}")
          Box {
            sx {
              timeSlotColumn()
            }
            Suspense {
              TimePicker {
                config = TimePickerConfig(
                  disabled = props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Details,
                  time = Date(clientDateRange.to),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Create) now else null,
                  onChange = { selectedTime ->
                    props.config.timeSlotTimeToOnChange(
                      selectedTime,
                      clientDateRange,
                    )
                  },
                )
              }
            }
          }
        }
      }
      if (props.config.accessManagementDetailsType !is AccessManagementDetailsConfig.Details) {
        gridItem(GridSize(xs = 1), key = "gridItemRemoveTimeslot${index}") {
          Tooltip {
            title = Strings.access_control_time_slot_remove.get().toReactNode()
            Box {
              component = span
              IconButton {
                sx {
                  marginLeft = 4.px
                  marginRight = 8.px
                }
                // At least one time slot must be set
                disabled = props.config.timeSlots.count() == 1
                Close()
                onClick = {
                  props.config.removeTimeSlotOnClick(
                    clientDateRange,
                  )
                }
              }
            }
          }
        }
      }
    }
    gridContainer(GridDirection.row, alignItems = AlignItems.center, spacing = 1, key = "gridContainerError$index") {
      gridItem(GridSize(xs = 12, sm = true)) {
        val fromDateTimeSlotError = props.config.fromDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
        if (fromDateTimeSlotError != null) {
          Typography {
            sx {
              color = theme.palette.error.main
            }
            +fromDateTimeSlotError.text
          }
        }
      }
      gridItem(GridSize(xs = 12, sm = true), key = "gridItemErrorText$index") {
        val toDateTimeSlotError = props.config.toDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
        if (toDateTimeSlotError != null) {
          Typography {
            sx {
              color = theme.palette.error.main
            }
            +toDateTimeSlotError.text
          }
        }
      }
      if (props.config.accessManagementDetailsType !is AccessManagementDetailsConfig.Details) {
        gridItem(GridSize(xs = 1), key = "gridItemNoDetails$index") {}
      }
    }
    spacer(24, key = "dateTimeSpacer${index}")
  }
}