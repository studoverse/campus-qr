package views.guestAccessManagement

import apiBase
import app.GlobalCss
import app.baseUrl
import com.studo.campusqr.common.ClientLocation
import com.studo.campusqr.common.NewGuestCheckIn
import kotlinext.js.js
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import util.Strings
import util.get
import views.common.centeredProgress
import views.common.networkErrorView
import views.common.renderLinearProgress
import views.common.spacer
import views.guestAccessManagement.guestAccessManagementOverview.GuestAccessManagementRowProps
import webcore.NetworkManager
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.materialUI.*
import kotlin.js.json

interface GuestAccessManagementDetailsProps : RProps {
  class Config(
    val onGuestCheckedIn: () -> Unit,
    val onShowSnackbar: (String) -> Unit
  )
  var classes: GuestAccessManagementDetailsClasses
  var config: Config
}

interface GuestAccessManagementDetailsState : RState {
  var locationFetchInProgress: Boolean
  var showProgress: Boolean
  var locationNameToLocationMap: Map<String, ClientLocation>

  var selectedLocation: ClientLocation?
  var selectedLocationTextFieldError: String

  var personEmailTextFieldValue: String
  var personEmailTextFieldError: String

  var seatInputValue: Int?
  var seatInputError: String
}

class GuestAccessManagementDetails :
  RComponent<GuestAccessManagementDetailsProps, GuestAccessManagementDetailsState>() {

  override fun GuestAccessManagementDetailsState.init() {
    locationFetchInProgress = false
    showProgress = false
    locationNameToLocationMap = emptyMap()

    selectedLocation = null
    selectedLocationTextFieldError = ""

    personEmailTextFieldValue = ""
    personEmailTextFieldError = ""

    seatInputValue = null
    seatInputError = ""
  }

  private fun fetchLocations() = launch {
    setState {
      locationFetchInProgress = true
    }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    setState {
      if (response != null) {
        locationNameToLocationMap = response.associateBy { it.name }
      }
      locationFetchInProgress = false
    }
  }

  private fun checkInGuest() = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<String>(
      url = "$baseUrl/location/${state.selectedLocation!!.id}${state.seatInputValue?.let { "-$it" }}/guestCheckIn",
      params = json("email" to state.personEmailTextFieldValue)
    )
    setState {
      showProgress = false
    }
    if (response == "ok") {
      props.config.onGuestCheckedIn()
    } else {
      props.config.onShowSnackbar(Strings.error_try_again.get())
    }
  }

  private fun validateInput(): Boolean {
    // Location has to be selected for creation
    if (state.selectedLocation == null) {
      setState {
        selectedLocationTextFieldError = Strings.access_control_please_select_location.get()
      }
      return false
    }

    if (state.personEmailTextFieldValue.isEmpty()) {
      setState {
        personEmailTextFieldError = "Email mustn't be empty!"
      }
      return false
    }

    if (state.selectedLocation?.seatCount != null && state.seatInputValue == null) {
      setState {
        seatInputError = "Please select a seat!"
      }
      return false
    }

    return true
  }

  override fun componentDidMount() {
    fetchLocations()
  }

  private fun RBuilder.renderSubmitButton() {
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
              checkInGuest()
            }
          }
          +"Add Guest"
        }
      }
    }
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
      muiAutocomplete {
        attrs.value = state.selectedLocation?.name ?: ""
        attrs.onChange = { _, target: String?, _ ->
          setState {
            selectedLocationTextFieldError = ""
            selectedLocation = target?.let { locationNameToLocationMap[it] }

            // User should re-select seat if needed
            seatInputValue = null
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
            attrs.label = Strings.location_name.get()
          }
        }
      }
      spacer(16)
      textField {
        attrs.error = state.personEmailTextFieldError.isNotEmpty()
        attrs.helperText = state.personEmailTextFieldError
        attrs.fullWidth = true
        attrs.variant = "outlined"
        attrs.label = Strings.email_address.get()
        attrs.value = state.personEmailTextFieldValue
        attrs.onChange = { event: Event ->
          val value = event.inputValue
          setState {
            personEmailTextFieldError = ""
            personEmailTextFieldValue = value
          }
        }
      }
      if (state.selectedLocation?.seatCount != null) {
        val options = state.selectedLocation?.seatCount?.let { seatCount ->
          (1..seatCount).map { it }.toTypedArray()
        } ?: emptyArray()
        spacer(16)
        muiAutocomplete {
          attrs.onChange = { _, target: Int?, _ ->
            setState {
              seatInputError = ""
              seatInputValue = target
            }
          }
          attrs.fullWidth = true
          attrs.multiple = false
          attrs.openOnFocus = true
          attrs.options = options
          attrs.value = state.seatInputValue
          attrs.getOptionLabel = { it.toString() }
          attrs.renderInput = { params: dynamic ->
            textField {
              attrs.error = state.seatInputError.isNotEmpty()
              attrs.helperText = state.seatInputError
              attrs.id = params.id
              attrs.InputProps = params.InputProps
              attrs.inputProps = params.inputProps
              attrs.disabled = params.disabled
              attrs.fullWidth = params.fullWidth
              attrs.variant = "outlined"
              attrs.label = Strings.report_checkin_seat.get()
            }
          }
        }
      }
      spacer(32)
      renderSubmitButton()
    }
  }
}

interface GuestAccessManagementDetailsClasses {
  // Keep in sync with GuestAccessManagementDetailsStyle!
  var addButton: String
  var form: String
}

private val GuestAccessManagementDetailsStyle = { theme: dynamic ->
  // Keep in sync with GuestAccessManagementDetailsClasses!
  js {
    addButton = js {
      marginBottom = 16
    }
    form = js {
      width = "100%"
    }
  }
}

private val styled =
  withStyles<GuestAccessManagementDetailsProps, GuestAccessManagementDetails>(GuestAccessManagementDetailsStyle)

fun RBuilder.renderGuestAccessManagementDetails(config: GuestAccessManagementDetailsProps.Config) = styled {
  attrs.config = config
}
  