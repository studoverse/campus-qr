package views.accessManagement

import app.GlobalCss
import app.appContextToInject
import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.payloads.*
import csstype.*
import js.objects.Object
import mui.icons.material.Close
import mui.material.*
import mui.material.Size
import mui.system.sx
import react.*
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.span
import util.Strings
import util.apiBase
import util.get
import views.common.*
import web.html.HTMLElement
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

// TODO: @mh Extract logic to AccessManagementDetailsController
val AccessManagementDetailsFc = FcWithCoroutineScope<AccessManagementDetailsProps> { props, launch ->
  var locationFetchInProgress: Boolean by useState(false)
  var showProgress: Boolean by useState(false)
  var locationNameToLocationMap: Map<String, ClientLocation> by useState(emptyMap())

  var selectedLocation: ClientLocation? by useState(null)
  var selectedLocationTextFieldError: String by useState("")

  var accessControlNoteTextFieldValue: String by useState("")
  var accessControlReasonTextFieldValue: String by useState("")
  var personEmailTextFieldValue: String by useState("")
  var permittedPeopleList: List<String> by useState(emptyList())
  var timeSlots: List<ClientDateRange> by useState(emptyList())

  var fromDateTimeSlotErrors: MutableList<TimeSlotError> by useState(mutableListOf())
  var toDateTimeSlotErrors: MutableList<TimeSlotError> by useState(mutableListOf())

  val appContext = useContext(appContextToInject)!!

  fun initFields(accessManagement: ClientAccessManagement?) {
    locationFetchInProgress = false
    showProgress = false
    locationNameToLocationMap = emptyMap()

    selectedLocation = null
    selectedLocationTextFieldError = ""

    accessControlNoteTextFieldValue = accessManagement?.note ?: ""
    accessControlReasonTextFieldValue = accessManagement?.reason ?: ""
    personEmailTextFieldValue = ""
    permittedPeopleList = accessManagement?.allowedEmails?.toList() ?: emptyList()

    fromDateTimeSlotErrors = mutableListOf()
    toDateTimeSlotErrors = mutableListOf()

    val fromDate = Date().addHours(1).with(minute = 0)
    timeSlots = accessManagement?.dateRanges?.toList() ?: listOf(
      ClientDateRange(
        from = fromDate.getTime(),
        to = fromDate.addHours(2).getTime()
      )
    )
  }

  fun PropertiesBuilder.timeSlotRow() {
    display = Display.flex
    flexDirection = FlexDirection.row
  }

  fun PropertiesBuilder.timeSlotColumn() {
    flex = Flex(number(0.0), number(1.0), 50.pct)
  }

  fun getPermittedEmailsFromTextField() =
    personEmailTextFieldValue.lowercase().split(*emailSeparators).filter { it.isNotEmpty() }.map { it.trim() }

  fun fetchLocations() = launch {
    locationFetchInProgress = true
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    if (response != null) {
      locationNameToLocationMap = response.associateBy { it.name }
      // Auto select current location
      val selectedLocationId = when (val config = props.config) {
        is AccessManagementDetailsConfig.Details -> config.accessManagement.locationId
        is AccessManagementDetailsConfig.Edit -> config.accessManagement.locationId
        is AccessManagementDetailsConfig.Create -> config.locationId
      }
      if (selectedLocationId != null) {
        selectedLocation = locationNameToLocationMap.values.firstOrNull { it.id == selectedLocationId }
      }
    }
    locationFetchInProgress = false
  }

  fun createAccessControl() = launch {
    showProgress = true
    val response = NetworkManager.post<String>(
      url = "$apiBase/access/create",
      body = NewAccess(
        locationId = selectedLocation!!.id,
        // Add state.getPermittedEmailsFromTextField(), to make sure that any un-submitted emails get added
        allowedEmails = permittedPeopleList + getPermittedEmailsFromTextField(),
        dateRanges = timeSlots,
        note = accessControlNoteTextFieldValue,
        reason = accessControlReasonTextFieldValue
      )
    )
    showProgress = false
    (props.config as AccessManagementDetailsConfig.Create).onCreated()
    props.config.dialogRef.current!!.closeDialog()
    val snackbarText = if (response == "ok") {
      Strings.access_control_created_successfully.get()
    } else {
      Strings.error_try_again.get()
    }
    appContext.showSnackbarText(snackbarText)
  }

  fun editAccessControl() = launch {
    showProgress = true
    val accessManagementId = (props.config as AccessManagementDetailsConfig.Edit).accessManagement.id
    val response = NetworkManager.post<String>(
      url = "$apiBase/access/$accessManagementId/edit",
      body = EditAccess(
        locationId = selectedLocation?.id,
        // Add state.getPermittedEmailsFromTextField(), to make sure that any un-submitted emails get added
        allowedEmails = permittedPeopleList + getPermittedEmailsFromTextField(),
        dateRanges = timeSlots,
        note = accessControlNoteTextFieldValue,
        reason = accessControlReasonTextFieldValue
      )
    )
    showProgress = false
    (props.config as AccessManagementDetailsConfig.Edit).onEdited(response == "ok")
    props.config.dialogRef.current!!.closeDialog()
  }

  fun validateInput(): Boolean {
    // Location has to be selected for creation
    if (props.config is AccessManagementDetailsConfig.Create && selectedLocation == null) {
      selectedLocationTextFieldError = Strings.access_control_please_select_location.get()
      return false
    }

    // At least one time slot has to be there in creation mode
    if (props.config is AccessManagementDetailsConfig.Create && timeSlots.isEmpty()) {
      // This shouldn't happen
      error("timeSlots empty: ${timeSlots}")
    }

    // Validate every timeslot
    timeSlots.forEach { timeSlot ->
      // End time cannot be before start time
      if (timeSlot.to < timeSlot.from) {
        toDateTimeSlotErrors.add(TimeSlotError(text = Strings.access_control_end_date_before_start_date.get(), timeSlot = timeSlot))
        return false
      }
    }

    return true
  }

  fun submitPermittedPeopleToState() {
    permittedPeopleList += getPermittedEmailsFromTextField()
    personEmailTextFieldValue = ""
  }

  fun ChildrenBuilder.renderLocationSelection() {
    Autocomplete<AutocompleteProps<String>> {
      disabled = props.config is AccessManagementDetailsConfig.Details
      value = selectedLocation?.name
      onChange = { _, value: Any?, _, _ ->
        value as String?
        selectedLocationTextFieldError = ""
        selectedLocation = value?.let { locationNameToLocationMap[it] }
      }
      openOnFocus = true
      options = locationNameToLocationMap.keys.toTypedArray()
      getOptionLabel = { it }
      renderInput = { params ->
        Fragment.create {
          TextField {
            Object.assign(this, params)
            error = selectedLocationTextFieldError.isNotEmpty()
            helperText = selectedLocationTextFieldError.toReactNode()
            fullWidth = true
            variant = FormControlVariant.outlined
            label = Strings.location_name.get().toReactNode()
          }
        }
      }
    }
  }

  fun ChildrenBuilder.renderNoteTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_note.get().toReactNode()
      value = accessControlNoteTextFieldValue
      onChange = { event ->
        val value = event.target.value
        accessControlNoteTextFieldValue = value
      }
    }
  }

  fun ChildrenBuilder.renderReasonTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_reason.get().toReactNode()
      value = accessControlReasonTextFieldValue
      onChange = { event ->
        val value = event.target.value
        accessControlReasonTextFieldValue = value
      }
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
            mui.icons.material.Add()
            onClick = {
              timeSlots = (timeSlots + ClientDateRange(timeSlots.last().from, timeSlots.last().to))
            }
          }
        }
      }
    }
    spacer(12)
    timeSlots.forEach { clientDateRange ->
      gridContainer(GridDirection.row, alignItems = AlignItems.center, spacing = 1) {
        gridItem(GridSize(xs = 12, sm = true)) {
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
                    timeSlots = timeSlots.map { timeSlot ->
                      if (timeSlot == clientDateRange) {
                        val startDateBefore = Date(clientDateRange.from)
                        val from = selectedDate.with(
                          hour = startDateBefore.getHours(),
                          minute = startDateBefore.getMinutes(),
                          second = startDateBefore.getSeconds(),
                          millisecond = startDateBefore.getMilliseconds()
                        ).coerceAtMost(inThreeYears).getTime()

                        // Default end date is start date + 2h
                        val to = if (from >= clientDateRange.to) {
                          selectedDate.coerceAtMost(inThreeYears).addHours(2).getTime()
                        } else clientDateRange.to
                        ClientDateRange(
                          from = from,
                          to = to
                        )
                      } else timeSlot
                    }
                  },
                )
              }
            }
            horizontalSpacer(12)
            Box {
              sx {
                timeSlotColumn()
              }
              timePicker(
                config = TimePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  time = Date(clientDateRange.from),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  onChange = { selectedTime ->
                    timeSlots = timeSlots.map { timeSlot ->
                      if (timeSlot == clientDateRange) {
                        val startDateBefore = Date(clientDateRange.from)
                        val from = selectedTime.with(
                          year = startDateBefore.getFullYear(),
                          month = startDateBefore.getMonth(),
                          day = startDateBefore.getDate()
                        ).getTime()
                        // Default end date is start date + 2h
                        val to = if (from >= clientDateRange.to) {
                          selectedTime.addHours(2).getTime()
                        } else clientDateRange.to
                        ClientDateRange(
                          from = from,
                          to = to
                        )
                      } else timeSlot
                    }
                  },
                )
              )
            }
          }
          spacer(16)
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
                    timeSlots = timeSlots.map { timeSlot ->
                      if (timeSlot == clientDateRange) {
                        val endDateBefore = Date(clientDateRange.to)
                        ClientDateRange(
                          from = clientDateRange.from,
                          to = selectedDate.with(
                            hour = endDateBefore.getHours(),
                            minute = endDateBefore.getMinutes(),
                            second = endDateBefore.getSeconds(),
                            millisecond = endDateBefore.getMilliseconds()
                          ).coerceAtMost(inThreeYears).getTime()
                        )
                      } else timeSlot
                    }
                  },
                )
              }
            }
            horizontalSpacer(12)
            Box {
              sx {
                timeSlotColumn()
              }
              timePicker(
                config = TimePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  time = Date(clientDateRange.to),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  onChange = { selectedTime ->
                    timeSlots = timeSlots.map { timeSlot ->
                      if (timeSlot == clientDateRange) {
                        val endDateBefore = Date(clientDateRange.to)
                        ClientDateRange(
                          from = clientDateRange.from,
                          to = selectedTime.with(
                            year = endDateBefore.getFullYear(),
                            month = endDateBefore.getMonth(),
                            day = endDateBefore.getDate()
                          ).getTime()
                        )
                      } else timeSlot
                    }
                  },
                )
              )
            }
          }
        }
        if (props.config !is AccessManagementDetailsConfig.Details) {
          gridItem(GridSize(xs = 1)) {
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
                  disabled = timeSlots.count() == 1
                  Close()
                  onClick = {
                    timeSlots = timeSlots.filter { it != clientDateRange }
                  }
                }
              }
            }
          }
        }
      }
      gridContainer(GridDirection.row, alignItems = AlignItems.center, spacing = 1) {
        gridItem(GridSize(xs = 12, sm = true)) {
          val fromDateTimeSlotError = fromDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
          if (fromDateTimeSlotError != null) {
            Typography {
              sx {
                color = theme.palette.error.main
              }
              +fromDateTimeSlotError.text
            }
          }
        }
        gridItem(GridSize(xs = 12, sm = true)) {
          val toDateTimeSlotError = toDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
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
          gridItem(GridSize(xs = 1)) {}
        }
      }
      spacer(24)
    }
  }

  fun ChildrenBuilder.renderPermittedPeople() {
    Typography {
      +Strings.access_control_permitted_people.get()
    }
    spacer(12)
    Box {
      component = form
      sx {
        width = 100.pct
      }
      onSubmit = { event ->
        if (props.config !is AccessManagementDetailsConfig.Details) {
          submitPermittedPeopleToState()
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
            value = personEmailTextFieldValue
            onChange = { event: ChangeEvent<HTMLElement> ->
              val value: String = event.target.value
              personEmailTextFieldValue = value
            }
          }

          Box {
            className = ClassName(GlobalCss.flexEnd)
            spacer()
            Button {
              size = Size.small
              color = ButtonColor.primary
              variant = ButtonVariant.outlined
              onClick = {
                submitPermittedPeopleToState()
              }
              +Strings.access_control_add_permitted_people.get()
            }
          }
        }
      }
    }

    if (permittedPeopleList.isNotEmpty()) {
      Table {
        TableHead {
          TableRow {
            TableCell { +Strings.email_address.get() }
            TableCell { }
          }
        }
        TableBody {
          permittedPeopleList.forEach { personIdentification ->
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
                      permittedPeopleList = permittedPeopleList.filter { it != personIdentification }
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
              // Reset errors from previous submit attempt
              fromDateTimeSlotErrors.clear()
              toDateTimeSlotErrors.clear()

              if (validateInput()) {
                when (props.config) {
                  is AccessManagementDetailsConfig.Create -> createAccessControl()
                  is AccessManagementDetailsConfig.Edit -> editAccessControl()
                  else -> Unit
                }
              }
            }
            +createButtonText
          }
        }
      }
    }
  }

  fun ChildrenBuilder.renderDetailsContent() {
    renderLocationSelection()
    spacer(16)
    renderNoteTextField()
    spacer(16)
    renderReasonTextField()
    spacer(24)
    renderTimeSlotPickers()
    renderPermittedPeople()
    spacer(32)
    renderActionButtons()
  }

  useEffectOnce {
    fetchLocations()
  }

  when (val config = props.config) {
    is AccessManagementDetailsConfig.Details -> initFields(config.accessManagement)
    is AccessManagementDetailsConfig.Edit -> initFields(config.accessManagement)
    is AccessManagementDetailsConfig.Create -> initFields(null)
  }

  renderMbLinearProgress(show = showProgress)

  if (!locationFetchInProgress && locationNameToLocationMap.isEmpty()) {
    networkErrorView()
    spacer(36)
  } else if (locationFetchInProgress) {
    centeredProgress()
    spacer(36)
  } else {
    renderDetailsContent()
  }
}
