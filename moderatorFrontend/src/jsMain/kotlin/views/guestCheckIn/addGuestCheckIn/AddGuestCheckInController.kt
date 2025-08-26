package views.guestCheckIn.addGuestCheckIn

import app.appContextToInject
import app.baseUrl
import com.studo.campusqr.common.payloads.CheckInData
import com.studo.campusqr.common.payloads.ClientLocation
import kotlinx.coroutines.Job
import react.useEffectOnce
import react.useState
import util.Strings
import util.apiBase
import util.get
import views.guestCheckIn.guestCheckInOverview.GuestCheckInOverviewController.Companion.locationIdWithSeat
import webcore.*
import kotlin.collections.associateBy
import kotlin.collections.get

data class AddGuestCheckInController(
  val locationFetchInProgress: Boolean,
  val showProgress: Boolean,
  val locationNameToLocationMap: Map<String, ClientLocation>,

  val selectedLocation: ClientLocation?,
  val selectedLocationTextFieldError: String,

  val personEmailTextFieldValue: String,
  val personEmailTextFieldError: String,

  val seatInputValue: Int?,
  val seatInputError: String,

  val fetchLocations: () -> Job,
  val validateInput: () -> Boolean,
  val checkInGuest: () -> Job,
  val locationAutoCompleteOnChange: AutocompleteOnChange<String>,
  val personEmailTextFieldOnChange: TextFieldOnChange,
  val seatInputAutocompleteOnChange: AutocompleteOnChange<Int>,
) {
  companion object {
    fun use(launch: Launch, props: AddGuestCheckInProps): AddGuestCheckInController {
      val appContext = react.use(appContextToInject)!!
      var locationFetchInProgress: Boolean by useState(false)
      var showProgress: Boolean by useState(false)
      var locationNameToLocationMap: Map<String, ClientLocation> by useState<Map<String, ClientLocation>>(emptyMap())

      var selectedLocation: ClientLocation? by useState<ClientLocation?>(null)
      var selectedLocationTextFieldError: String by useState("")

      var personEmailTextFieldValue: String by useState("")
      var personEmailTextFieldError: String by useState("")

      var seatInputValue: Int? by useState(null)
      var seatInputError: String by useState("")

      fun fetchLocations() = launch {
        locationFetchInProgress = true
        val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
        if (response != null) {
          locationNameToLocationMap = response.associateBy { it.name }
        }
        locationFetchInProgress = false
      }

      fun checkInGuest() = launch {
        showProgress = true
        val locationId = locationIdWithSeat(selectedLocation!!.id, seatInputValue)
        val response = NetworkManager.post<String>(
          url = "$baseUrl/location/$locationId/guestCheckIn",
          body = CheckInData(email = personEmailTextFieldValue)
        )
        showProgress = false
        when (response) {
          "ok" -> {
            props.config.onGuestCheckedIn()
            props.config.dialogRef.current!!.closeDialog()
          }

          "forbidden_email" -> appContext.showSnackbarText(Strings.invalid_email.get())
          else -> appContext.showSnackbarText(Strings.error_try_again.get())
        }
      }

      fun validateInput(): Boolean {
        // Location has to be selected for creation
        if (selectedLocation == null) {
          selectedLocationTextFieldError = Strings.access_control_please_select_location.get()
          return false
        }

        if (personEmailTextFieldValue.isEmpty()) {
          personEmailTextFieldError = Strings.guest_checkin_email_must_not_be_empty.get()
          return false
        }

        if (selectedLocation?.seatCount != null && seatInputValue == null) {
          seatInputError = Strings.guest_checkin_select_seat.get()
          return false
        }

        return true
      }

      val locationAutoCompleteOnChange: AutocompleteOnChange<String> = { _, value, _, _ ->
        selectedLocationTextFieldError = ""
        selectedLocation = value.let { locationNameToLocationMap[it] }

        // User should re-select seat if needed
        seatInputValue = null
      }

      useEffectOnce {
        fetchLocations()
      }

      val personEmailTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        personEmailTextFieldError = ""
        personEmailTextFieldValue = value
      }

      val seatInputAutocompleteOnChange: AutocompleteOnChange<Int> = { _, value, _, _ ->
        value as Int?
        seatInputError = ""
        seatInputValue = value
      }

      return AddGuestCheckInController(
        locationFetchInProgress = locationFetchInProgress,
        showProgress = showProgress,
        locationNameToLocationMap = locationNameToLocationMap,
        selectedLocation = selectedLocation,
        selectedLocationTextFieldError = selectedLocationTextFieldError,
        personEmailTextFieldValue = personEmailTextFieldValue,
        personEmailTextFieldError = personEmailTextFieldError,
        seatInputValue = seatInputValue,
        seatInputError = seatInputError,
        fetchLocations = ::fetchLocations,
        validateInput = ::validateInput,
        checkInGuest = ::checkInGuest,
        locationAutoCompleteOnChange = locationAutoCompleteOnChange,
        personEmailTextFieldOnChange = personEmailTextFieldOnChange,
        seatInputAutocompleteOnChange = seatInputAutocompleteOnChange,
      )
    }
  }
}