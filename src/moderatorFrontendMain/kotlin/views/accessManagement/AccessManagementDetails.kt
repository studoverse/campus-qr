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
import views.common.spacer
import webcore.*
import webcore.extensions.*
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
  var accessControlNoteTextFieldValue: String
  var personIdentificationTextFieldValue: String
  var permittedPeopleList: List<String>

  var fromDate: Date
  var toDate: Date
}

class AddLocation(props: AccessManagementDetailsProps) : RComponent<AccessManagementDetailsProps, AccessManagementDetailsState>(props) {

  override fun AccessManagementDetailsState.init(props: AccessManagementDetailsProps) {
    showProgress = false
    locationNameToLocationMap = emptyMap()
    selectedLocation = null
    accessControlNoteTextFieldValue = ""
    personIdentificationTextFieldValue = ""
    permittedPeopleList = emptyList()

    val now = Date()
    fromDate = now.addHours(1).with(minute = 0)
    toDate = fromDate.addHours(1)
  }

  override fun componentDidMount() {
    fetchLocations()
  }

  private fun fetchLocations() = launch {
    setState { showProgress = true }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    console.log(response)
    setState {
      if (response != null) {
        locationNameToLocationMap = response.associateBy { it.name }
      } else {
        // TODO: Show network error
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
                allowedEmails = state.permittedPeopleList,
                dateRanges = listOf(
                    ClientDateRange(
                        Date().getTime().toLong(),
                        Date().getTime().toLong()
                    )
                ),
                note = state.accessControlNoteTextFieldValue,
                reason = ""
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
                locationId = null, // TODO: Where am I supposed to get location id from?
                allowedEmails = state.permittedPeopleList,
                dateRanges = listOf(
                    ClientDateRange(
                        Date().getTime().toLong(),
                        Date().getTime().toLong()
                    )
                ),
                note = state.accessControlNoteTextFieldValue,
                reason = "" // TODO: What is reason?
            )
        )
    )
    setState {
      showProgress = false
    }
    (props.config as Config.Edit).onEdited(response == "ok")
  }

  private fun validateInput(): Boolean {
    /*
    if (state.locationTextFieldValue.isEmpty()) {
      setState {
        locationTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.name.get())
      }
      return false
    }

     */
    return true
  }

  override fun RBuilder.render() {

    muiAutocomplete {
      attrs.disabled = props.config is Config.Details
      attrs.value = state.selectedLocation?.name ?: ""
      attrs.onChange = { _, target: Array<String>?, _ ->
        setState {
          selectedLocation = target?.first()?.let { locationNameToLocationMap[it] }
        }
      }
      attrs.openOnFocus = true
      attrs.options = state.locationNameToLocationMap.keys.toTypedArray()
      attrs.getOptionLabel = { it }
      attrs.renderInput = { params: dynamic ->
        textField {
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

    spacer(24)

    typography {
      +"Time slots"
    }
    spacer(12)
    gridContainer(GridDirection.ROW, alignItems = "center", spacing = 1) {
      gridItem(GridSize(xs = 6)) {
        muiDateTimePicker {
          attrs.format = "dd.MM.yyyy, hh:mm"
          attrs.ampm = false
          attrs.inputVariant = "outlined"
          attrs.fullWidth = true
          attrs.label = "From"
          attrs.value = state.fromDate
          attrs.onChange = {
            setState {
              fromDate = it.toJSDate()
            }
          }
        }
      }
      gridItem(GridSize(xs = 6)) {
        muiDateTimePicker {
          attrs.format = "dd.MM.yyyy, hh:mm"
          attrs.ampm = false
          attrs.inputVariant = "outlined"
          attrs.fullWidth = true
          attrs.label = "To"
          attrs.value = state.toDate
          attrs.onChange = {
            setState {
              toDate = it.toJSDate()
            }
          }
        }
      }
    }
    spacer(24)
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
  }
}

private val styled = withStyles<AccessManagementDetailsProps, AddLocation>(AddLocationStyle)

fun RBuilder.renderAccessManagementDetails(config: Config) = styled {
  attrs.config = config
}
