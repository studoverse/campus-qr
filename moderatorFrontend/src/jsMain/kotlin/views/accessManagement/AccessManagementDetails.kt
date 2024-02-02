package views.accessManagement

import app.AppContext
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

sealed class AccessManagementDetailsConfig(val dialogRef: RefObject<MbDialog>) {
  class Create(val locationId: String?, dialogRef: RefObject<MbDialog>, val onCreated: () -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Edit(val accessManagement: ClientAccessManagement, dialogRef: RefObject<MbDialog>, val onEdited: (Boolean) -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Details(val accessManagement: ClientAccessManagement, dialogRef: RefObject<MbDialog>) :
    AccessManagementDetailsConfig(dialogRef)
}

class TimeSlotError(val text: String, val timeSlot: ClientDateRange)

external interface AccessManagementDetailsProps : Props {
  var config: AccessManagementDetailsConfig
}

external interface AccessManagementDetailsState : State {
  var locationFetchInProgress: Boolean
  var showProgress: Boolean
  var locationNameToLocationMap: Map<String, ClientLocation>

  var selectedLocation: ClientLocation?
  var selectedLocationTextFieldError: String

  var accessControlNoteTextFieldValue: String
  var accessControlReasonTextFieldValue: String
  var personEmailTextFieldValue: String
  var permittedPeopleList: List<String>
  var timeSlots: List<ClientDateRange>

  var fromDateTimeSlotErrors: MutableList<TimeSlotError>
  var toDateTimeSlotErrors: MutableList<TimeSlotError>
}

@Suppress("UPPER_BOUND_VIOLATED")
class AddLocation(props: AccessManagementDetailsProps) :
  RComponent<AccessManagementDetailsProps, AccessManagementDetailsState>(props) {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AddLocation::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun AccessManagementDetailsState.init(props: AccessManagementDetailsProps) {
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

    when (val config = props.config) {
      is AccessManagementDetailsConfig.Details -> initFields(config.accessManagement)
      is AccessManagementDetailsConfig.Edit -> initFields(config.accessManagement)
      else -> initFields(null)
    }
  }

  override fun componentDidMount() {
    fetchLocations()
  }

  private fun fetchLocations() = launch {
    setState {
      locationFetchInProgress = true
    }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    setState {
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
  }

  private fun createAccessControl() = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<String>(
      url = "$apiBase/access/create",
      body = NewAccess(
        locationId = state.selectedLocation!!.id,
        // Add state.getPermittedEmailsFromTextField(), to make sure that any un-submitted emails get added
        allowedEmails = state.permittedPeopleList + state.getPermittedEmailsFromTextField(),
        dateRanges = state.timeSlots,
        note = state.accessControlNoteTextFieldValue,
        reason = state.accessControlReasonTextFieldValue
      )
    )
    setState {
      showProgress = false
    }
    (props.config as AccessManagementDetailsConfig.Create).onCreated()
    props.config.dialogRef.current!!.closeDialog()
    val snackbarText = if (response == "ok") {
      Strings.access_control_created_successfully.get()
    } else {
      Strings.error_try_again.get()
    }
    appContext.showSnackbar(snackbarText)
  }

  private fun editAccessControl() = launch {
    setState { showProgress = true }
    val accessManagementId = (props.config as AccessManagementDetailsConfig.Edit).accessManagement.id
    val response = NetworkManager.post<String>(
      url = "$apiBase/access/$accessManagementId/edit",
      body = EditAccess(
        locationId = state.selectedLocation?.id,
        // Add state.getPermittedEmailsFromTextField(), to make sure that any un-submitted emails get added
        allowedEmails = state.permittedPeopleList + state.getPermittedEmailsFromTextField(),
        dateRanges = state.timeSlots,
        note = state.accessControlNoteTextFieldValue,
        reason = state.accessControlReasonTextFieldValue
      )
    )
    setState {
      showProgress = false
    }
    (props.config as AccessManagementDetailsConfig.Edit).onEdited(response == "ok")
    props.config.dialogRef.current!!.closeDialog()
  }

  private fun validateInput(): Boolean {
    // Location has to be selected for creation
    if (props.config is AccessManagementDetailsConfig.Create && state.selectedLocation == null) {
      setState {
        selectedLocationTextFieldError = Strings.access_control_please_select_location.get()
      }
      return false
    }

    // At least one time slot has to be there in creation mode
    if (props.config is AccessManagementDetailsConfig.Create && state.timeSlots.isEmpty()) {
      // This shouldn't happen
      error("timeSlots empty: ${state.timeSlots}")
    }

    // Validate every timeslot
    state.timeSlots.forEach { timeSlot ->
      // End time cannot be before start time
      if (timeSlot.to < timeSlot.from) {
        setState {
          toDateTimeSlotErrors.add(TimeSlotError(text = Strings.access_control_end_date_before_start_date.get(), timeSlot = timeSlot))
        }
        return false
      }
    }

    return true
  }

  private fun AccessManagementDetailsState.getPermittedEmailsFromTextField() =
    personEmailTextFieldValue.lowercase().split(*emailSeparators).filter { it.isNotEmpty() }.map { it.trim() }

  private fun submitPermittedPeopleToState() = setState {
    permittedPeopleList += getPermittedEmailsFromTextField()
    personEmailTextFieldValue = ""
  }

  private fun ChildrenBuilder.renderLocationSelection() {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST") // Workaround for https://youtrack.jetbrains.com/issue/KT-60185
    Autocomplete {
      disabled = props.config is AccessManagementDetailsConfig.Details
      value = state.selectedLocation?.name ?: ""
      onChange = { _, target: String?, _, _ ->
        setState {
          selectedLocationTextFieldError = ""
          selectedLocation = target?.let { locationNameToLocationMap[it] }
        }
      }
      openOnFocus = true
      (this as AutocompleteProps<String>).options = state.locationNameToLocationMap.keys.toTypedArray()
      (this as AutocompleteProps<String>).getOptionLabel = { it }
      renderInput = { params ->
        Fragment.create {
          TextField {
            Object.assign(this, params)
            error = state.selectedLocationTextFieldError.isNotEmpty()
            helperText = state.selectedLocationTextFieldError.toReactNode()
            fullWidth = true
            variant = FormControlVariant.outlined
            label = Strings.location_name.get().toReactNode()
          }
        }
      }
    }
  }

  private fun ChildrenBuilder.renderNoteTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_note.get().toReactNode()
      value = state.accessControlNoteTextFieldValue
      onChange = { event ->
        val value = event.target.value
        setState {
          accessControlNoteTextFieldValue = value
        }
      }
    }
  }

  private fun ChildrenBuilder.renderReasonTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_reason.get().toReactNode()
      value = state.accessControlReasonTextFieldValue
      onChange = { event ->
        val value = event.target.value
        setState {
          accessControlReasonTextFieldValue = value
        }
      }
    }
  }

  private fun ChildrenBuilder.renderTimeSlotPickers() {
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
              setState {
                timeSlots = (timeSlots + ClientDateRange(timeSlots.last().from, timeSlots.last().to))
              }
            }
          }
        }
      }
    }
    spacer(12)
    state.timeSlots.forEach { clientDateRange ->
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
              datePicker(
                config = DatePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  date = Date(clientDateRange.from),
                  label = Strings.access_control_from.get(),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  max = inThreeYears,
                  onChange = { selectedDate, _ ->
                    setState {
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
                    }
                  },
                )
              )
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
                    setState {
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
              datePicker(
                config = DatePickerConfig(
                  disabled = props.config is AccessManagementDetailsConfig.Details,
                  date = Date(clientDateRange.to),
                  label = Strings.access_control_to.get(),
                  fullWidth = true,
                  variant = FormControlVariant.outlined,
                  min = if (props.config is AccessManagementDetailsConfig.Create) now else null,
                  max = inThreeYears,
                  onChange = { selectedDate, _ ->
                    setState {
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
                    }
                  },
                )
              )
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
                    setState {
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
                  disabled = state.timeSlots.count() == 1
                  Close()
                  onClick = {
                    setState {
                      timeSlots = timeSlots.filter { it != clientDateRange }
                    }
                  }
                }
              }
            }
          }
        }
      }
      gridContainer(GridDirection.row, alignItems = AlignItems.center, spacing = 1) {
        gridItem(GridSize(xs = 12, sm = true)) {
          val fromDateTimeSlotError = state.fromDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
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
          val toDateTimeSlotError = state.toDateTimeSlotErrors.singleOrNull { it.timeSlot == clientDateRange }
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

  private fun ChildrenBuilder.renderPermittedPeople() {
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
            value = state.personEmailTextFieldValue
            onChange = { event: ChangeEvent<HTMLElement> ->
              val value: String = event.target.value
              setState {
                personEmailTextFieldValue = value
              }
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

    if (state.permittedPeopleList.isNotEmpty()) {
      Table {
        TableHead {
          TableRow {
            TableCell { +Strings.email_address.get() }
            TableCell { }
          }
        }
        TableBody {
          state.permittedPeopleList.forEach { personIdentification ->
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
                      setState {
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
  }

  private fun ChildrenBuilder.renderActionButtons() {
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
              setState {
                fromDateTimeSlotErrors.clear()
                toDateTimeSlotErrors.clear()
              }
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

  private fun ChildrenBuilder.renderDetailsContent() {
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

  override fun ChildrenBuilder.render() {
    renderMbLinearProgress(show = state.showProgress)

    if (!state.locationFetchInProgress && state.locationNameToLocationMap.isEmpty()) {
      networkErrorView()
      spacer(36)
    } else if (state.locationFetchInProgress) {
      centeredProgress()
      spacer(36)
    } else {
      renderDetailsContent()
    }
  }

  private fun PropertiesBuilder.timeSlotRow() {
    display = Display.flex
    flexDirection = FlexDirection.row
  }

  private fun PropertiesBuilder.timeSlotColumn() {
    flex = Flex(number(0.0), number(1.0), 50.pct)
  }
}

fun ChildrenBuilder.renderAccessManagementDetails(config: AccessManagementDetailsConfig) {
  AddLocation::class.react {
    this.config = config
  }
}
