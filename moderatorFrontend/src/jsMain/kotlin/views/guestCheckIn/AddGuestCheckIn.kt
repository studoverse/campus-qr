package views.guestCheckIn

import app.AppContext
import app.GlobalCss
import app.appContextToInject
import app.baseUrl
import com.studo.campusqr.common.payloads.CheckInData
import com.studo.campusqr.common.payloads.ClientLocation
import web.cssom.*
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.apiBase
import util.get
import views.common.CenteredProgressFc
import views.common.MbLinearProgressFc
import views.common.networkErrorView
import views.common.spacer
import webcore.*
import webcore.extensions.launch

class AddGuestCheckInConfig(
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onGuestCheckedIn: () -> Unit,
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

class AddGuestCheckIn : RComponent<AddGuestCheckInProps, AddGuestCheckInState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AddGuestCheckIn::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

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
      "ok" -> {
        props.config.onGuestCheckedIn()
        props.config.dialogRef.current!!.closeDialog()
      }
      "forbidden_email" -> appContext.showSnackbarText(Strings.invalid_email.get())
      else -> appContext.showSnackbarText(Strings.error_try_again.get())
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
    MbLinearProgressFc { show = state.showProgress }

    if (!state.locationFetchInProgress && state.locationNameToLocationMap.isEmpty()) {
      networkErrorView()
      spacer(36)
    } else if (state.locationFetchInProgress) {
      CenteredProgressFc {}
      spacer(36)
    } else {
      Autocomplete<AutocompleteProps<String>> {
        value = state.selectedLocation?.name
        onChange = { _, target: Any?, _, _ ->
          target as String?
          setState {
            selectedLocationTextFieldError = ""
            selectedLocation = target.let { locationNameToLocationMap[it] }

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
            helperText = state.selectedLocationTextFieldError.toReactNode()
            fullWidth = true
            variant = FormControlVariant.outlined
            label = Strings.location_name.get().toReactNode()
          }

        }
      }
      spacer(16)
      TextField {
        error = state.personEmailTextFieldError.isNotEmpty()
        helperText = state.personEmailTextFieldError.toReactNode()
        fullWidth = true
        variant = FormControlVariant.outlined
        label = Strings.email_address.get().toReactNode()
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
          onChange = { _, target: Any?, _, _ ->
            target as Int?
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
              helperText = state.seatInputError.toReactNode()
              variant = FormControlVariant.outlined
              label = Strings.report_checkin_seat.get().toReactNode()
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

fun ChildrenBuilder.renderAddGuestCheckIn(config: AddGuestCheckInConfig) {
  AddGuestCheckIn::class.react {
    this.config = config
  }
}
