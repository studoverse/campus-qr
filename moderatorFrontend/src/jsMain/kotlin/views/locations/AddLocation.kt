package views.locations

import web.cssom.*
import app.GlobalCss
import com.studo.campusqr.common.LocationAccessType
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.CreateOrUpdateLocationData
import js.objects.jso
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.apiBase
import util.get
import util.localizedString
import views.common.MbLinearProgressFc
import views.common.spacer
import web.html.InputType
import webcore.*

sealed class AddLocationConfig(val dialogRef: MutableRefObject<MbDialogRef>, val onFinished: (response: String?) -> Unit) {
  class Create(dialogRef: MutableRefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)

  class Edit(val location: ClientLocation, dialogRef: MutableRefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)
}

external interface AddLocationProps : Props {
  var config: AddLocationConfig
}

val AddLocation = FcWithCoroutineScope<AddLocationProps> { props, launch ->
  var locationCreationInProgress: Boolean by useState(false)
  var locationTextFieldValue: String by useState((props.config as? AddLocationConfig.Edit)?.location?.name ?: "")
  var locationTextFieldError: String by useState("")
  var locationAccessType: LocationAccessType by useState(
    (props.config as? AddLocationConfig.Edit)?.location?.accessType ?: LocationAccessType.FREE
  )
  var locationSeatCount: Int? by useState((props.config as? AddLocationConfig.Edit)?.location?.seatCount)

  // TODO: @mh Figure out if this works to migrate the unsaved changes dialog.
  val navigable = useMemo(locationTextFieldValue, locationAccessType, locationSeatCount) {
    console.log("navigable is created!") // TODO: @mh Remove after testing
    fun shouldNavigateAway(): Boolean {
      console.log("locationTextFieldValue: ", locationTextFieldValue) // TODO: @mh Remove after testing
      return when (val config = props.config) {
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
    }

    object : NavigateAwayObservable {
      override fun shouldNavigateAway(): Boolean = shouldNavigateAway()
    }
  }

  // TODO: @mh Move to controller.
  fun createOrUpdateLocation() = launch {
    locationCreationInProgress = true
    val url = when (val config = props.config) {
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
    props.config.onFinished(response)
    if (response == "ok") {
      props.config.dialogRef.current!!.closeDialog()
    }
  }

  fun validateInput(): Boolean {
    if (locationTextFieldValue.isEmpty()) {
      locationTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.name.get())

      return false
    }
    return true
  }

  useEffectWithCleanup(navigable) {
    NavigationHandler.navigateAwayListeners.add(navigable)

    onCleanup {
      NavigationHandler.navigateAwayListeners.remove(navigable)
    }
  }

  MbLinearProgressFc { show = locationCreationInProgress }

  TextField {
    error = locationTextFieldError.isNotEmpty()
    helperText = locationTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    label = Strings.location_name.get().toReactNode()
    value = locationTextFieldValue
    inputProps = jso {
      maxLength = 40 // Make sure names stay printable
    }
    onChange = { event ->
      val value = event.target.value
      locationTextFieldValue = value
      locationTextFieldError = ""
    }
  }

  spacer(16, key = "locationName")

  Box {
    sx {
      display = Display.flex
      justifyContent = JustifyContent.center
      alignItems = AlignItems.center
      fontFamily = string("'Roboto', Arial, sans-serif")
    }
    FormControl {
      fullWidth = true
      InputLabel {
        +Strings.user_permissions.get()
      }
      variant = FormControlVariant.outlined
      Select<SelectProps<String>> {
        value = locationAccessType.name
        onChange = { event, _ ->
          locationAccessType = LocationAccessType.valueOf(event.target.value)
        }
        variant = SelectVariant.outlined
        label = Strings.location_access_type.get().toReactNode()

        LocationAccessType.entries.forEach { accessType ->
          MenuItem {
            key = accessType.name
            value = accessType.name
            +accessType.localizedString.get()
          }
        }
      }
    }
  }

  spacer(16, key = "locationPermissionsSpacer")

  TextField {
    placeholder = Strings.undefined.get()
    fullWidth = true
    variant = FormControlVariant.outlined
    type = InputType.number
    label = Strings.location_number_of_seats_hint.get().toReactNode()
    value = locationSeatCount?.toString() ?: ""
    onChange = { event ->
      val value = event.target.value
      locationSeatCount = value.toIntOrNull()?.coerceIn(1, 10_000)
    }
  }

  spacer(32, key = "seatCountSpacer")

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
            createOrUpdateLocation()
          }
        }
        +if (props.config is AddLocationConfig.Create) {
          Strings.location_add.get()
        } else {
          Strings.location_update.get()
        }
      }
    }
  }
}
