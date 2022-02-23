package views.guestCheckIn

import app.GlobalCss
import app.baseUrl
import com.studo.campusqr.common.payloads.CheckInData
import com.studo.campusqr.common.payloads.ClientLocation
import kotlinext.js.js
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import util.Strings
import util.apiBase
import util.get
import views.common.centeredProgress
import views.common.networkErrorView
import views.common.renderLinearProgress
import views.common.spacer
import webcore.NetworkManager
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.materialUI.muiAutocomplete
import webcore.materialUI.muiButton
import webcore.materialUI.textField
import webcore.materialUI.withStyles

class AddGuestCheckInConfig(
  val onGuestCheckedIn: () -> Unit,
  val onShowSnackbar: (String) -> Unit
)

external interface AddGuestCheckInProps : RProps {
  var classes: AddGuestCheckInClasses
  var config: AddGuestCheckInConfig
}

external interface AddGuestCheckInState : RState {
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

class AddGuestCheckIn : RComponent<AddGuestCheckInProps, AddGuestCheckInState>() {

  override fun AddGuestCheckInState.init() {
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
    val locationId = locationIdWithSeat(state.selectedLocation!!.id, state.seatInputValue)
    val response = NetworkManager.post<String>(
      url = "$baseUrl/location/$locationId/guestCheckIn",
      body = CheckInData(email = state.personEmailTextFieldValue)
    )
    setState {
      showProgress = false
    }
    when (response) {
      "ok" -> props.config.onGuestCheckedIn()
      "forbidden_email" -> props.config.onShowSnackbar(Strings.invalid_email.get())
      else -> props.config.onShowSnackbar(Strings.error_try_again.get())
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
        personEmailTextFieldError = Strings.guest_checkin_email_must_not_be_empty.get()
      }
      return false
    }

    if (state.selectedLocation?.seatCount != null && state.seatInputValue == null) {
      setState {
        seatInputError = Strings.guest_checkin_select_seat.get()
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
          +Strings.guest_checkin_add_guest.get()
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

// If seat is not null, id gets appended with '-' to locationId
fun locationIdWithSeat(locationId: String, seat: Int?) = "$locationId${seat?.let { "-$it" } ?: ""}"

interface AddGuestCheckInClasses {
  var addButton: String
  var form: String
}

private val style = { _: dynamic ->
  js {
    addButton = js {
      marginBottom = 16
    }
    form = js {
      width = "100%"
    }
  }
}

private val styled = withStyles<AddGuestCheckInProps, AddGuestCheckIn>(style)

fun RBuilder.renderAddGuestCheckIn(config: AddGuestCheckInConfig) = styled {
  attrs.config = config
}