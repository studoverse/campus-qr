package views.accessManagement

import app.GlobalCss
import app.appContextToInject
import com.studo.campusqr.common.payloads.*
import csstype.*
import mui.icons.material.Add
import mui.icons.material.Close
import mui.material.*
import mui.material.Size
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.span
import util.Strings
import util.get
import views.common.*
import web.cssom.*
import webcore.*
import webcore.extensions.*
import kotlin.js.Date

sealed class AccessManagementDetailsConfig(val dialogRef: RefObject<MbDialogRef>) {
  class Create(val locationId: String?, dialogRef: MutableRefObject<MbDialogRef>, val onCreated: () -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Edit(val accessManagement: ClientAccessManagement, dialogRef: MutableRefObject<MbDialogRef>, val onEdited: (Boolean) -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Details(val accessManagement: ClientAccessManagement, dialogRef: MutableRefObject<MbDialogRef>) :
    AccessManagementDetailsConfig(dialogRef)
}

class TimeSlotError(val text: String, val timeSlot: ClientDateRange)

external interface AccessManagementDetailsProps : Props {
  var config: AccessManagementDetailsConfig
}

val AccessManagementDetailsFc = FcWithCoroutineScope<AccessManagementDetailsProps> { props, launch ->
  val controller = AccessManagementDetailsController.useAccessManagementDetailsController(config = props.config, launch = launch)
  val appContext = useContext(appContextToInject)!!

  fun PropertiesBuilder.timeSlotRow() {
    display = Display.flex
    flexDirection = FlexDirection.row
  }

  fun PropertiesBuilder.timeSlotColumn() {
    flex = Flex(number(0.0), number(1.0), 50.pct)
  }

  // TODO: @mh All the functions need to be own components, so they don't clutter the component here.
  fun ChildrenBuilder.renderNoteTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_note.get().toReactNode()
      value = controller.accessControlNoteTextFieldValue
      onChange = controller.noteTextFieldOnChange
    }
  }

  fun ChildrenBuilder.renderReasonTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_reason.get().toReactNode()
      value = controller.accessControlReasonTextFieldValue
      onChange = controller.reasonTextFieldOnChange
    }
  }

  fun ChildrenBuilder.renderTimeSlotPickers() {
    val theme = appContext.theme
    val now = Date()
    val inThreeYears = now.addYears(3)
    Box {
      className = ClassName(GlobalCss.flex)
      Typography {
        +Strings.access_control_time_slots.get()
      }
      if (props.config !is AccessManagementDetailsConfig.Details) {
        Tooltip {
          title = Strings.access_control_time_slot_add.get().toReactNode()
          IconButton {
            sx {
              padding = 0.px
              marginLeft = 8.px
            }
            Add()
            onClick = controller.addTimeSlotOnClick
          }
        }
      }
    }
    spacer(12, key = "timeSlotsSpacer1")
    controller.timeSlots.forEachIndexed { index, clientDateRange ->
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
              DatePickerFc {
                config = DatePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  date = Date(clientDateRange.from),
                  label = Strings.access_control_from.get(),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  max = inThreeYears,
                  onChange = { selectedDate, _ ->
                    controller.timeSlotDateFromOnChange(
                      selectedDate,
                      clientDateRange,
                      now,
                      inThreeYears,
                    )
                  },
                )
              }
            }
            horizontalSpacer(12, key = "timeSlotColumnFromSpacer${index}")
            Box {
              sx {
                timeSlotColumn()
              }
              TimePickerFc {
                config = TimePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  time = Date(clientDateRange.from),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  onChange = { selectedTime ->
                    controller.timeSlotTimeFromOnChange(
                      selectedTime,
                      clientDateRange,
                    )
                  },
                )
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
              DatePickerFc {
                config = DatePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  date = Date(clientDateRange.to),
                  label = Strings.access_control_to.get(),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  max = inThreeYears,
                  onChange = { selectedDate, _ ->
                    controller.timeSlotDateToOnChange(
                      selectedDate,
                      clientDateRange,
                      now,
                      inThreeYears,
                    )
                  },
                )
              }
            }
            horizontalSpacer(12, key = "timeSlotColumnToSpacer${index}")
            Box {
              sx {
                timeSlotColumn()
              }
              TimePickerFc {
                config = TimePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  time = Date(clientDateRange.to),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  onChange = { selectedTime ->
                    controller.timeSlotTimeToOnChange(
                      selectedTime,
                      clientDateRange,
                    )
                  },
                )
              }
            }
          }
        }
        if (props.config !is AccessManagementDetailsConfig.Details) {
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
                  disabled = controller.timeSlots.count() == 1
                  Close()
                  onClick = {
                    controller.removeTimeSlotOnClick(
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
          val fromDateTimeSlotError = controller.fromDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
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
          val toDateTimeSlotError = controller.toDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
          if (toDateTimeSlotError != null) {
            Typography {
              sx {
                color = theme.palette.error.main
              }
              +toDateTimeSlotError.text
            }
          }
        }
        if (props.config !is AccessManagementDetailsConfig.Details) {
          gridItem(GridSize(xs = 1), key = "gridItemNoDetails$index") {}
        }
      }
      spacer(24, key = "dateTimeSpacer${index}")
    }
  }

  fun ChildrenBuilder.renderPermittedPeople() {
    Typography {
      +Strings.access_control_permitted_people.get()
    }
    spacer(12, key = "permittedPeopleSpacer1")
    Box {
      component = form
      sx {
        width = 100.pct
      }
      onSubmit = { event ->
        if (props.config !is AccessManagementDetailsConfig.Details) {
          controller.submitPermittedPeopleToState()
        }
        event.preventDefault()
        event.stopPropagation()
      }
      if (props.config !is AccessManagementDetailsConfig.Details) {
        Box {
          className = ClassName(GlobalCss.flex)
          TextField {
            disabled = props.config is AccessManagementDetailsConfig.Details
            helperText = Strings.access_control_add_permitted_people_tip.get().toReactNode()
            fullWidth = true
            variant = FormControlVariant.outlined
            label = Strings.email_address.get().toReactNode()
            value = controller.personEmailTextFieldValue
            onChange = controller.addPermittedPeopleOnChange
          }

          Box {
            className = ClassName(GlobalCss.flexEnd)
            spacer(key = "addPersonSpacer1")
            Button {
              size = Size.small
              color = ButtonColor.primary
              variant = ButtonVariant.outlined
              onClick = {
                controller.submitPermittedPeopleToState()
              }
              +Strings.access_control_add_permitted_people.get()
            }
          }
        }
      }
    }

    if (controller.permittedPeopleList.isNotEmpty()) {
      Table {
        TableHead {
          TableRow {
            TableCell { +Strings.email_address.get() }
            TableCell { }
          }
        }
        TableBody {
          controller.permittedPeopleList.forEach { personIdentification ->
            TableRow {
              TableCell {
                +personIdentification
              }

              TableCell {
                align = TableCellAlign.right
                if (props.config !is AccessManagementDetailsConfig.Details) {
                  IconButton {
                    Close()
                    onClick = {
                      controller.removePermittedPeopleOnClick(
                        personIdentification
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  fun ChildrenBuilder.renderActionButtons() {
    val createButtonText = when (props.config) {
      is AccessManagementDetailsConfig.Create -> Strings.access_control_create.get()
      is AccessManagementDetailsConfig.Edit -> Strings.access_control_save.get()
      is AccessManagementDetailsConfig.Details -> ""
    }
    if (createButtonText.isNotEmpty()) {
      Box {
        className = ClassName(GlobalCss.flex)
        Box {
          sx {
            marginBottom = 16.px
          }
          className = ClassName(GlobalCss.flexEnd)
          Button {
            sx {
              marginRight = 16.px
            }
            +Strings.cancel.get()
            variant = ButtonVariant.text
            onClick = {

              props.config.dialogRef.current!!.closeDialog()
            }
          }
          Button {
            variant = ButtonVariant.contained
            color = ButtonColor.primary
            onClick = {
              controller.createAccessControlOnClick(
                props.config,
              )
            }
            +createButtonText
          }
        }
      }
    }
  }

  fun ChildrenBuilder.renderDetailsContent() {
    AccessManagementLocationSelectionFc {
      this.config = AccessManagementLocationSelectionConfig(
        isAutocompleteDisabled = props.config is AccessManagementDetailsConfig.Details,
        selectedLocation = controller.selectedLocation,
        selectedLocationTextFieldError = controller.selectedLocationTextFieldError,
        locationNameToLocationMap = controller.locationNameToLocationMap,
        locationSelectionOnChange = controller.locationSelectionOnChange,
      )
    }
    spacer(16, key = "renderDetailsContentSpacer1")
    renderNoteTextField()
    spacer(16, key = "renderDetailsContentSpacer2")
    renderReasonTextField()
    spacer(24, key = "renderDetailsContentSpacer3")
    renderTimeSlotPickers()
    renderPermittedPeople()
    spacer(32, key = "renderDetailsContentSpacer4")
    renderActionButtons()
  }

  MbLinearProgressFc { show = controller.showProgress }

  if (!controller.locationFetchInProgress && controller.locationNameToLocationMap.isEmpty()) {
    networkErrorView()
    spacer(36)
  } else if (controller.locationFetchInProgress) {
    CenteredProgressFc {}
    spacer(36)
  } else {
    renderDetailsContent()
  }
}
