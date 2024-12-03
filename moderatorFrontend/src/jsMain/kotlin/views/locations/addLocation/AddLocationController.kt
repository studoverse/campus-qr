package views.locations.addLocation

import com.studo.campusqr.common.LocationAccessType
import com.studo.campusqr.common.payloads.CreateOrUpdateLocationData
import react.useCallback
import react.useState
import util.Strings
import util.get
import util.apiBase
import webcore.ButtonOnClick
import webcore.Launch
import webcore.NetworkManager
import webcore.extensions.format
import webcore.NavigationHandler.useShouldNavigateAway
import webcore.SelectOnChange
import webcore.TextFieldOnChange

data class AddLocationController(
  val locationCreationInProgress: Boolean,
  val locationTextFieldValue: String,
  val locationTextFieldError: String,
  val locationAccessType: LocationAccessType,
  val locationSeatCount: Int?,
  val locationNameOnChange: TextFieldOnChange,
  val locationAccessTypeOnChange: SelectOnChange,
  val locationSeatCountOnChange: TextFieldOnChange,
  val createOrEditLocationIfInputValid: ButtonOnClick,
) {
  companion object {
    fun use(config: AddLocationConfig, launch: Launch): AddLocationController {
      var locationCreationInProgress: Boolean by useState(false)
      var locationTextFieldValue: String by useState((config as? AddLocationConfig.Edit)?.location?.name ?: "")
      var locationTextFieldError: String by useState("")
      var locationAccessType: LocationAccessType by useState(
        (config as? AddLocationConfig.Edit)?.location?.accessType ?: LocationAccessType.FREE
      )
      var locationSeatCount: Int? by useState((config as? AddLocationConfig.Edit)?.location?.seatCount)

      useShouldNavigateAway(
        useCallback(locationTextFieldValue, locationAccessType, locationSeatCount) {
          when (config) {
            is AddLocationConfig.Create -> {
              locationTextFieldValue.isEmpty() &&
                  locationAccessType == LocationAccessType.FREE &&
                  locationSeatCount == null
            }

            is AddLocationConfig.Edit -> {
              locationTextFieldValue == config.location.name &&
                  locationAccessType == config.location.accessType &&
                  locationSeatCount == config.location.seatCount
            }
          }
        },
      )

      fun createOrUpdateLocation() = launch {
        locationCreationInProgress = true
        val url = when (config) {
          is AddLocationConfig.Create -> "$apiBase/location/create"
          is AddLocationConfig.Edit -> "$apiBase/location/${config.location.id}/edit"
        }
        val response = NetworkManager.post<String>(
          url = url,
          body = CreateOrUpdateLocationData(
            name = locationTextFieldValue,
            accessType = locationAccessType,
            seatCount = locationSeatCount
          )
        )
        locationCreationInProgress = false
        config.onFinished(response)
        if (response == "ok") {
          config.dialogRef.current!!.closeDialog()
        }
      }

      fun validateInput(): Boolean {
        if (locationTextFieldValue.isEmpty()) {
          locationTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.name.get())

          return false
        }
        return true
      }

      val locationNameOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        locationTextFieldValue = value
        locationTextFieldError = ""
      }

      val locationAccessTypeOnChange: SelectOnChange = { event, _ ->
        locationAccessType = LocationAccessType.valueOf(event.target.value)
      }

      val locationSeatCountOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        locationSeatCount = value.toIntOrNull()?.coerceIn(1, 10_000)
      }

      val createOrEditLocationIfInputValid: ButtonOnClick = {
        if (validateInput()) {
          createOrUpdateLocation()
        }
      }

      return AddLocationController(
        locationCreationInProgress = locationCreationInProgress,
        locationTextFieldValue = locationTextFieldValue,
        locationTextFieldError = locationTextFieldError,
        locationAccessType = locationAccessType,
        locationSeatCount = locationSeatCount,
        locationNameOnChange = locationNameOnChange,
        locationAccessTypeOnChange = locationAccessTypeOnChange,
        locationSeatCountOnChange = locationSeatCountOnChange,
        createOrEditLocationIfInputValid = createOrEditLocationIfInputValid,
      )
    }
  }
}
