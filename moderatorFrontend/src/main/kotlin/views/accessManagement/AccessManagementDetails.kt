package views.accessManagement

import app.GlobalCss
import com.studo.campusqr.common.emailSeparators
import com.studo.campusqr.common.payloads.*
import kotlinext.js.js
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import react.dom.form
import react.dom.span
import util.Strings
import util.apiBase
import util.get
import views.common.*
import webcore.*
import webcore.extensions.*
import webcore.materialUI.*
import kotlin.js.Date

sealed class AccessManagementDetailsConfig {
  class Create(val locationId: String?, val onCreated: (Boolean) -> Unit) : AccessManagementDetailsConfig()
  class Edit(val accessManagement: ClientAccessManagement, val onEdited: (Boolean) -> Unit) : AccessManagementDetailsConfig()
  class Details(val accessManagement: ClientAccessManagement) : AccessManagementDetailsConfig()
}

external interface AccessManagementDetailsProps : RProps {
  var config: AccessManagementDetailsConfig
  var classes: AccessManagementDetailsClasses
}

external interface AccessManagementDetailsState : RState {
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

  var fromDateTextFieldError: String
  var toDateTextFieldError: String
}

class AddLocation(props: AccessManagementDetailsProps) : RComponent<AccessManagementDetailsProps, AccessManagementDetailsState>(props) {

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

      fromDateTextFieldError = ""
      toDateTextFieldError = ""

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
    (props.config as AccessManagementDetailsConfig.Create).onCreated(response == "ok")
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
          toDateTextFieldError = Strings.access_control_end_date_before_start_date.get()
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

  private fun RBuilder.renderLocationSelection() {
    muiAutocomplete {
      attrs.disabled = props.config is AccessManagementDetailsConfig.Details
      attrs.value = state.selectedLocation?.name ?: ""
      attrs.onChange = { _, target: String?, _ ->
        setState {
          selectedLocationTextFieldError = ""
          selectedLocation = target?.let { locationNameToLocationMap[it] }
        }
      }
      attrs.openOnFocus = true
      attrs.options = state.locationNameToLocationMap.keys.toTypedArray()
      attrs.getOptionLabel = { it }
      attrs.renderInput = { params: dynamic ->
        textField {
          attrs.error = state.selectedLocationTextFieldError.isNotEmpty()
          attrs.helperText = state.selectedLocationTextFieldError
          attrs.id = params.id
          attrs.InputProps = params.InputProps
          attrs.inputProps = params.inputProps
          attrs.disabled = params.disabled
          attrs.fullWidth = params.fullWidth
          attrs.fullWidth = true
          attrs.variant = TextFieldVariant.OUTLINED.value
          attrs.label = Strings.location_name.get()
        }
      }
    }
  }

  private fun RBuilder.renderNoteTextField() {
    textField {
      attrs.disabled = props.config is AccessManagementDetailsConfig.Details
      attrs.fullWidth = true
      attrs.variant = TextFieldVariant.OUTLINED.value
      attrs.label = Strings.access_control_note.get()
      attrs.value = state.accessControlNoteTextFieldValue
      attrs.onChange = { event: Event ->
        val value = event.inputValue
        setState {
          accessControlNoteTextFieldValue = value
        }
      }
    }
  }

  private fun RBuilder.renderReasonTextField() {
    textField {
      attrs.disabled = props.config is AccessManagementDetailsConfig.Details
      attrs.fullWidth = true
      attrs.variant = TextFieldVariant.OUTLINED.value
      attrs.label = Strings.access_control_reason.get()
      attrs.value = state.accessControlReasonTextFieldValue
      attrs.onChange = { event: Event ->
        val value = event.inputValue
        setState {
          accessControlReasonTextFieldValue = value
        }
      }
    }

  }

  private fun RBuilder.renderTimeSlotPickers() {
    val now = Date()
    val inThreeYears = now.addYears(3)
    div(GlobalCss.flex) {
      typography {
        +Strings.access_control_time_slots.get()
      }
      if (props.config !is AccessManagementDetailsConfig.Details) {
        muiTooltip {
          attrs.title = Strings.access_control_time_slot_add.get()
          iconButton {
            attrs.classes = js {
              root = props.classes.addTimeSlotButton
            }
            addIcon {}
            attrs.onClick = {
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
      gridContainer(GridDirection.ROW, alignItems = "center", spacing = 1) {
        gridItem(GridSize(xs = 12, sm = true)) {
          div(props.classes.timeSlotRow) {
            div(props.classes.timeSlotColumn) {
              datePicker(
                disabled = props.config is AccessManagementDetailsConfig.Details,
                date = Date(clientDateRange.from),
                label = Strings.access_control_from.get(),
                fullWidth = true,
                variant = TextFieldVariant.OUTLINED,
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
            }
            horizontalSpacer(12)
            div(props.classes.timeSlotColumn) {
              timePicker(
                disabled = props.config is AccessManagementDetailsConfig.Details,
                time = Date(clientDateRange.from),
                fullWidth = true,
                variant = TextFieldVariant.OUTLINED,
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
            }
          }
          spacer(16)
          div(props.classes.timeSlotRow) {
            div(props.classes.timeSlotColumn) {
              datePicker(
                disabled = props.config is AccessManagementDetailsConfig.Details,
                date = Date(clientDateRange.to),
                label = Strings.access_control_to.get(),
                fullWidth = true,
                variant = TextFieldVariant.OUTLINED,
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
            }
            horizontalSpacer(12)
            div(props.classes.timeSlotColumn) {
              timePicker(
                disabled = props.config is AccessManagementDetailsConfig.Details,
                time = Date(clientDateRange.to),
                fullWidth = true,
                variant = TextFieldVariant.OUTLINED,
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
            }
          }
        }
        if (props.config !is AccessManagementDetailsConfig.Details) {
          gridItem(GridSize(xs = 1)) {
            muiTooltip {
              attrs.title = Strings.access_control_time_slot_remove.get()
              span {
                iconButton {
                  attrs.classes = js {
                    root = props.classes.removeTimeSlotButton
                  }
                  // At least one time slot must be set
                  attrs.disabled = state.timeSlots.count() == 1
                  closeIcon {}
                  attrs.onClick = {
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
      gridContainer(GridDirection.ROW, alignItems = "center", spacing = 1) {
        gridItem(GridSize(xs = 12, sm = true)) {
          if (state.fromDateTextFieldError.isNotEmpty()) {
            typography {
              attrs.color = "error"
              +state.fromDateTextFieldError
            }
          }
        }
        gridItem(GridSize(xs = 12, sm = true)) {
          if (state.toDateTextFieldError.isNotEmpty()) {
            typography {
              attrs.color = "error"
              +state.toDateTextFieldError
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

  private fun RBuilder.renderPermittedPeople() {
    typography {
      +Strings.access_control_permitted_people.get()
    }
    spacer(12)
    form(props.classes.form) {
      attrs.onSubmitFunction = { event ->
        if (props.config !is AccessManagementDetailsConfig.Details) {
          submitPermittedPeopleToState()
        }
        event.preventDefault()
        event.stopPropagation()
      }
      if (props.config !is AccessManagementDetailsConfig.Details) {
        div(GlobalCss.flex) {
          textField {
            attrs.disabled = props.config is AccessManagementDetailsConfig.Details
            attrs.helperText = Strings.access_control_add_permitted_people_tip.get()
            attrs.fullWidth = true
            attrs.variant = TextFieldVariant.OUTLINED.value
            attrs.label = Strings.email_address.get()
            attrs.value = state.personEmailTextFieldValue
            attrs.onChange = { event: Event ->
              val value = event.inputValue
              setState {
                personEmailTextFieldValue = value
              }
            }
          }

          div(GlobalCss.flexEnd) {
            spacer()
            muiButton {
              attrs.size = "small"
              attrs.color = "primary"
              attrs.variant = "outlined"
              attrs.onClick = {
                submitPermittedPeopleToState()
              }
              +Strings.access_control_add_permitted_people.get()
            }
          }
        }
      }
    }

    if (state.permittedPeopleList.isNotEmpty()) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.email_address.get() }
            mTableCell { }
          }
        }
        mTableBody {
          state.permittedPeopleList.forEach { personIdentification ->
            mTableRow {
              mTableCell {
                +personIdentification
              }

              mTableCell {
                attrs.align = "right"
                if (props.config !is AccessManagementDetailsConfig.Details) {
                  iconButton {
                    closeIcon {}
                    attrs.onClick = {
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

  private fun RBuilder.renderSubmitButton() {
    val createButtonText = when (props.config) {
      is AccessManagementDetailsConfig.Create -> Strings.access_control_create.get()
      is AccessManagementDetailsConfig.Edit -> Strings.access_control_save.get()
      is AccessManagementDetailsConfig.Details -> ""
    }
    if (createButtonText.isNotEmpty()) {
      div(GlobalCss.flex) {
        div(GlobalCss.flexEnd) {
          muiButton {
            attrs.classes = js {
              root = props.classes.addButton
            }
            attrs.variant = "contained"
            attrs.color = "primary"
            attrs.onClick = {
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

  private fun RBuilder.renderDetailsContent() {
    renderLocationSelection()
    spacer(16)
    renderNoteTextField()
    spacer(16)
    renderReasonTextField()
    spacer(24)
    renderTimeSlotPickers()
    renderPermittedPeople()
    spacer(32)
    renderSubmitButton()
  }

  override fun RBuilder.render() {
    renderLinearProgress(state.showProgress)

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
}

external interface AccessManagementDetailsClasses {
  var addButton: String
  var form: String
  var addTimeSlotButton: String
  var removeTimeSlotButton: String
  var timeSlotRow: String
  var timeSlotColumn: String
}

private val style = { _: dynamic ->
  js {
    addButton = js {
      marginBottom = 16
    }
    form = js {
      width = "100%"
    }
    addTimeSlotButton = js {
      padding = 0
      marginLeft = 8
    }
    removeTimeSlotButton = js {
      marginLeft = 4
      marginRight = 8
    }
    timeSlotRow = js {
      display = "flex"
      flexDirection = "row"
    }
    timeSlotColumn = js {
      flex = "50%"
    }
  }
}

private val styled = withStyles<AccessManagementDetailsProps, AddLocation>(style)

fun RBuilder.renderAccessManagementDetails(config: AccessManagementDetailsConfig) = styled {
  attrs.config = config
}
