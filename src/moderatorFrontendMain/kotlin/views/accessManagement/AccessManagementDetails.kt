package views.accessManagement

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.*
import kotlinext.js.js
import kotlinx.html.js.onSubmitFunction
import muiDateTimePicker
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import react.dom.form
import util.Strings
import util.get
import views.accessManagement.AccessManagementDetailsProps.Config
import views.common.renderLinearProgress
import views.common.spacer
import webcore.*
import webcore.extensions.addHours
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.extensions.with
import webcore.materialUI.*
import kotlin.js.Date

interface AccessManagementDetailsProps : RProps {
  sealed class Config {
    class Create(val onCreated: (Boolean) -> Unit) : Config()
    class Edit(val accessManagementId: String, val onEdited: (Boolean) -> Unit) : Config()
    class Details(val accessManagementId: String) : Config()
  }

  var config: Config
  var classes: AddLocationClasses
}

interface AccessManagementDetailsState : RState {
  var showProgress: Boolean
  var locationNameToLocationMap: Map<String, ClientLocation>

  var selectedLocation: ClientLocation?
  var selectedLocationTextFieldError: String

  var accessControlNoteTextFieldValue: String
  var accessControlReasonTextFieldValue: String
  var personIdentificationTextFieldValue: String
  var permittedPeopleList: List<String>
  var timeSlots: List<ClientDateRange>
  var toDateTextFieldError: String
}

class AddLocation(props: AccessManagementDetailsProps) : RComponent<AccessManagementDetailsProps, AccessManagementDetailsState>(props) {

  override fun AccessManagementDetailsState.init(props: AccessManagementDetailsProps) {
    showProgress = false
    locationNameToLocationMap = emptyMap()

    selectedLocation = null
    selectedLocationTextFieldError = ""

    accessControlNoteTextFieldValue = ""
    accessControlReasonTextFieldValue = ""
    personIdentificationTextFieldValue = ""
    permittedPeopleList = emptyList()
    toDateTextFieldError = ""

    val fromDate = Date().addHours(1).with(minute = 0)
    timeSlots = listOf(
        ClientDateRange(
            from = fromDate.getTime(),
            to = fromDate.addHours(2).getTime()
        )
    )
  }

  override fun componentDidMount() {
    fetchLocations()
  }

  private fun fetchLocations() = launch {
    setState { showProgress = true }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    setState {
      if (response != null) {
        locationNameToLocationMap = response.associateBy { it.name }
      } else {
        // TODO: Show error
      }
      showProgress = false
    }
  }

  private fun createAccessControl() = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<String>(
        url = "$apiBase/access/create",
        json = JSON.stringify(
            NewAccess(
                locationId = state.selectedLocation!!.id,
                allowedEmails = state.permittedPeopleList.toTypedArray(),
                dateRanges = state.timeSlots.toTypedArray(),
                note = state.accessControlNoteTextFieldValue,
                reason = state.accessControlReasonTextFieldValue
            )
        )
    )
    setState {
      showProgress = false
    }
    (props.config as Config.Create).onCreated(response == "ok")
  }

  private fun editAccessControl() = launch {
    setState { showProgress = true }
    val accessManagementId = (props.config as Config.Edit).accessManagementId
    val response = NetworkManager.post<String>(
        url = "$apiBase/access/$accessManagementId/edit",
        json = JSON.stringify(
            EditAccess(
                locationId = state.selectedLocation?.id,
                allowedEmails = state.permittedPeopleList.toTypedArray(),
                dateRanges = state.timeSlots.toTypedArray(),
                note = state.accessControlNoteTextFieldValue,
                reason = state.accessControlReasonTextFieldValue
            )
        )
    )
    setState {
      showProgress = false
    }
    (props.config as Config.Edit).onEdited(response == "ok")
  }

  private fun validateInput(): Boolean {
    // Location has to be selected for creation
    if (props.config is Config.Create && state.selectedLocation == null) {
      setState {
        selectedLocationTextFieldError = "Please select a location!"
      }
      return false
    }

    // At least one time slot has to be there in creation mode
    if (props.config is Config.Create && state.timeSlots.isEmpty()) {
      // This shouldn't happen
      error("timeSlots empty: ${state.timeSlots}")
    }

    // Validate every timeslot
    state.timeSlots.forEach { timeSlot ->
      // End time cannot be before start time
      if (timeSlot.to < timeSlot.from) {
        // This shouldn't happen
        setState {
          toDateTextFieldError = "End date cannot happen before start date!"
        }
        return false
      }
    }

    // Note, reason, permitted people are all optional fields
    return true
  }

  override fun RBuilder.render() {
    renderLinearProgress(state.showProgress)

    muiAutocomplete {
      attrs.disabled = props.config is Config.Details
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
          attrs.variant = "outlined"
          attrs.label = "Location"
        }
      }
    }

    spacer(16)

    textField {
      attrs.fullWidth = true
      attrs.variant = "outlined"
      attrs.label = Strings.access_control_note.get()
      attrs.value = state.accessControlNoteTextFieldValue
      attrs.inputProps = js {
        maxLength = 40 // Make sure names stay printable
      }
      attrs.onChange = { event: Event ->
        val value = event.inputValue
        setState {
          accessControlNoteTextFieldValue = value
        }
      }
    }

    spacer(16)

    textField {
      attrs.fullWidth = true
      attrs.variant = "outlined"
      attrs.label = Strings.access_control_reason.get()
      attrs.value = state.accessControlReasonTextFieldValue
      attrs.inputProps = js {
        maxLength = 40 // Make sure names stay printable
      }
      attrs.onChange = { event: Event ->
        val value = event.inputValue
        setState {
          accessControlReasonTextFieldValue = value
        }
      }
    }

    spacer(24)

    div(GlobalCss.flex) {
      typography {
        +"Time slots"
      }
      muiTooltip {
        attrs.title = "Add a time slot"
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

    spacer(12)

    // TODO: start-end logic
    state.timeSlots.forEach { clientDateRange ->
      gridContainer(GridDirection.ROW, alignItems = "center", spacing = 1) {
        gridItem(GridSize(xs = 12, sm = true)) {
          muiDateTimePicker {
            attrs.format = "dd.MM.yyyy, hh:mm"
            attrs.ampm = false
            attrs.inputVariant = "outlined"
            attrs.fullWidth = true
            attrs.label = "From"
            attrs.disablePast = true
            attrs.value = Date(clientDateRange.from)
            attrs.onChange = { selectedDateTime ->
              setState {
                timeSlots = timeSlots.map { timeSlot ->
                  if (timeSlot == clientDateRange) {
                    // If start is after end, update end = start + 2h
                    val from = selectedDateTime.toJSDate().getTime()
                    val to = if (from >= clientDateRange.to) {
                      selectedDateTime.toJSDate().addHours(2).getTime()
                    } else clientDateRange.to
                    ClientDateRange(
                        from = from,
                        to = to
                    )
                  } else timeSlot
                }
              }
            }
          }
        }
        gridItem(GridSize(xs = 12, sm = true)) {
          muiDateTimePicker {
            attrs.format = "dd.MM.yyyy, hh:mm"
            attrs.ampm = false
            attrs.inputVariant = "outlined"
            attrs.fullWidth = true
            attrs.label = "To"
            attrs.disablePast = true
            attrs.value = Date(clientDateRange.to)
            attrs.onChange = { selectedDateTime ->
              setState {
                timeSlots = timeSlots.map { timeSlot ->
                  if (timeSlot == clientDateRange) {
                    ClientDateRange(
                        from = clientDateRange.from,
                        to = selectedDateTime.toJSDate().getTime()
                    )
                  } else timeSlot
                }
              }
            }
          }
        }
        gridItem(GridSize(xs = 1)) {
          muiTooltip {
            attrs.title = "Remove this timeslot"
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
        if (state.toDateTextFieldError.isNotEmpty()) {
          typography {
            attrs.color = "error"
            +state.toDateTextFieldError
          }
        }
      }
      spacer(24)
    }

    typography {
      +"Permitted people"
    }
    spacer(12)
    form(props.classes.form) {
      fun submitPermittedPerson() = setState {
        permittedPeopleList += personIdentificationTextFieldValue.split(*emailSeparators).filter { it.isNotEmpty() }.map { it.trim() }
        personIdentificationTextFieldValue = ""
      }
      attrs.onSubmitFunction = { event ->
        submitPermittedPerson()
        event.preventDefault()
        event.stopPropagation()
      }
      div(GlobalCss.flex) {
        textField {
          attrs.helperText = "Tip: You can add multiple people, by using comma a sperator"
          attrs.fullWidth = true
          attrs.variant = "outlined"
          attrs.label = "Email or student id"
          attrs.value = state.personIdentificationTextFieldValue
          attrs.inputProps = js {
            maxLength = 40 // Make sure names stay printable
          }
          attrs.onChange = { event: Event ->
            val value = event.inputValue
            setState {
              personIdentificationTextFieldValue = value
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
              submitPermittedPerson()
            }
            +"Add permitted person"
          }
        }
      }
    }

    if (state.permittedPeopleList.isNotEmpty()) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +"Person identification" }
            mTableCell { }
          }
        }
        mTableBody {
          state.permittedPeopleList.forEach { personIdentification ->
            mTableRow {
              mTableCell {
                +personIdentification
              }

              if (props.config !is Config.Details) {
                mTableCell {
                  attrs.align = "right"
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

    spacer(32)

    val createButtonText = when (props.config) {
      is Config.Create -> "Create access control"
      is Config.Edit -> "Save access control"
      is Config.Details -> ""
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
                  is Config.Create -> createAccessControl()
                  is Config.Edit -> editAccessControl()
                }
              }
            }
            +createButtonText
          }
        }
      }
    }
  }
}

interface AddLocationClasses {
  // Keep in sync with AddLocationStyle!
  var addButton: String
  var form: String
  var addTimeSlotButton: String
  var removeTimeSlotButton: String
}

private val AddLocationStyle = { theme: dynamic ->
  // Keep in sync with AddLocationClasses!
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
  }
}

private val styled = withStyles<AccessManagementDetailsProps, AddLocation>(AddLocationStyle)

fun RBuilder.renderAccessManagementDetails(config: Config) = styled {
  attrs.config = config
}
