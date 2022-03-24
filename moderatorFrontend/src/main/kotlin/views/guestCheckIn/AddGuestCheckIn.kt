package views.guestCheckIn

import app.GlobalCss
import app.baseUrl
import com.studo.campusqr.common.payloads.CheckInData
import com.studo.campusqr.common.payloads.ClientLocation
import csstype.ClassName
import csstype.px
import kotlinx.js.jso
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.apiBase
import util.get
import views.common.centeredProgress
import views.common.networkErrorView
import views.common.renderMbLinearProgress
import views.common.spacer
import webcore.*
import webcore.extensions.launch

class AddGuestCheckInConfig(
  val onGuestCheckedIn: () -> Unit,
  val onShowSnackbar: (String) -> Unit
)

external interface AddGuestCheckInProps : Props {
  var config: AddGuestCheckInConfig
}

external interface AddGuestCheckInState : State {
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

@Suppress("UPPER_BOUND_VIOLATED")
private class AddGuestCheckIn : RComponent<AddGuestCheckInProps, AddGuestCheckInState>() {

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

  private fun ChildrenBuilder.renderSubmitButton() {
    Box {
      className = ClassName(GlobalCss.flex)
      Box {
        className = ClassName(GlobalCss.flexEnd)
        Button {
          sx {
            marginBottom = 16.px
          }
          variant = ButtonVariant.contained
          color = ButtonColor.primary
          onClick = {
            if (validateInput()) {
              checkInGuest()
            }
          }
          +Strings.guest_checkin_add_guest.get()
        }
      }
    }
  }

  override fun ChildrenBuilder.render() {
    renderMbLinearProgress { show = state.showProgress }

    if (!state.locationFetchInProgress && state.locationNameToLocationMap.isEmpty()) {
      networkErrorView()
      spacer(36)
    } else if (state.locationFetchInProgress) {
      centeredProgress()
      spacer(36)
    } else {
      Autocomplete<AutocompleteProps<String>> {
        value = state.selectedLocation?.name ?: ""
        onChange = { _, target: String?, _, _ ->
          setState {
            selectedLocationTextFieldError = ""
            selectedLocation = target?.let { locationNameToLocationMap[it] }

            // User should re-select seat if needed
            seatInputValue = null
          }
        }
        openOnFocus = true
        options = state.locationNameToLocationMap.keys.toTypedArray()
        getOptionLabel = { it }
        renderInput = { params ->
          TextField.create {
            +params
            error = state.selectedLocationTextFieldError.isNotEmpty()
            helperText = ReactNode(state.selectedLocationTextFieldError)
            fullWidth = true
            variant = FormControlVariant.outlined
            label = ReactNode(Strings.location_name.get())
          }

        }
      }
      spacer(16)
      TextField<OutlinedTextFieldProps> {
        error = state.personEmailTextFieldError.isNotEmpty()
        helperText = ReactNode(state.personEmailTextFieldError)
        fullWidth = true
        variant = FormControlVariant.outlined()
        label = ReactNode(Strings.email_address.get())
        value = state.personEmailTextFieldValue
        onChange = { event ->
          val value = event.target.value
          setState {
            personEmailTextFieldError = ""
            personEmailTextFieldValue = value
          }
        }
      }
      if (state.selectedLocation?.seatCount != null) {
        val options = (1..state.selectedLocation?.seatCount!!).map { it }.toTypedArray()
        spacer(16)
        Autocomplete<AutocompleteProps<Int>> {
          onChange = { _, target: Int?, _, _ ->
            setState {
              seatInputError = ""
              seatInputValue = target
            }
          }
          fullWidth = true
          multiple = false
          openOnFocus = true
          this.options = options
          value = state.seatInputValue
          getOptionLabel = { it.toString() }
          renderInput = { params ->
            TextField.create {
              +params
              error = state.seatInputError.isNotEmpty()
              helperText = ReactNode(state.seatInputError)
              variant = FormControlVariant.outlined
              label = ReactNode(Strings.report_checkin_seat.get())
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

fun ChildrenBuilder.renderAddGuestCheckIn(handler: AddGuestCheckInProps.() -> Unit) {
  AddGuestCheckIn::class.react {
    +jso(handler)
  }
}
