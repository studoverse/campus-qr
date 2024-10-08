package views.locations

import web.cssom.*
import app.AppContext
import app.GlobalCss
import app.appContextToInject
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
import webcore.extensions.launch

sealed class AddLocationConfig(val dialogRef: MutableRefObject<MbDialogRef>, val onFinished: (response: String?) -> Unit) {
  class Create(dialogRef: MutableRefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)

  class Edit(val location: ClientLocation, dialogRef: MutableRefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)
}

external interface AddLocationProps : Props {
  var config: AddLocationConfig
}

external interface AddLocationState : State {
  var locationCreationInProgress: Boolean
  var locationTextFieldValue: String
  var locationTextFieldError: String
  var locationAccessType: LocationAccessType
  var locationSeatCount: Int?
}

class AddLocation(props: AddLocationProps) : RComponent<AddLocationProps, AddLocationState>(props), NavigateAwayObservable {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AddLocation::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun componentDidMount() {
    NavigationHandler.navigateAwayListeners.add(this)
  }

  override fun componentWillUnmount() {
    NavigationHandler.navigateAwayListeners.remove(this)
  }

  override fun AddLocationState.init(props: AddLocationProps) {
    locationCreationInProgress = false
    locationTextFieldError = ""
    locationTextFieldValue = (props.config as? AddLocationConfig.Edit)?.location?.name ?: ""
    locationAccessType = (props.config as? AddLocationConfig.Edit)?.location?.accessType ?: LocationAccessType.FREE
    locationSeatCount = (props.config as? AddLocationConfig.Edit)?.location?.seatCount
  }

  override fun shouldNavigateAway(): Boolean {
    return when (val config = props.config) {
      is AddLocationConfig.Create -> {
        state.locationTextFieldValue.isEmpty() &&
            state.locationAccessType == LocationAccessType.FREE &&
            state.locationSeatCount == null
      }

      is AddLocationConfig.Edit -> {
        state.locationTextFieldValue == config.location.name &&
            state.locationAccessType == config.location.accessType &&
            state.locationSeatCount == config.location.seatCount
      }
    }
  }

  private fun createOrUpdateLocation() = launch {
    setState { locationCreationInProgress = true }
    val url = when (val config = props.config) {
      is AddLocationConfig.Create -> "$apiBase/location/create"
      is AddLocationConfig.Edit -> "$apiBase/location/${config.location.id}/edit"
    }
    val response = NetworkManager.post<String>(
      url = url,
      body = CreateOrUpdateLocationData(
        name = state.locationTextFieldValue,
        accessType = state.locationAccessType,
        seatCount = state.locationSeatCount
      )
    )
    setState {
      locationCreationInProgress = false
    }
    props.config.onFinished(response)
    if (response == "ok") {
      props.config.dialogRef.current!!.closeDialog()
    }
  }

  private fun validateInput(): Boolean {
    if (state.locationTextFieldValue.isEmpty()) {
      setState {
        locationTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.name.get())
      }
      return false
    }
    return true
  }

  override fun ChildrenBuilder.render() {
    MbLinearProgressFc { show = state.locationCreationInProgress }

    TextField {
      error = state.locationTextFieldError.isNotEmpty()
      helperText = state.locationTextFieldError.toReactNode()
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.location_name.get().toReactNode()
      value = state.locationTextFieldValue
      inputProps = jso {
        maxLength = 40 // Make sure names stay printable
      }
      onChange = { event ->
        val value = event.target.value
        setState {
          locationTextFieldValue = value
          locationTextFieldError = ""
        }
      }
    }

    spacer(16)

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
          value = state.locationAccessType.name
          onChange = { event, _ ->
            setState {
              locationAccessType = LocationAccessType.valueOf(event.target.value)
            }
          }
          variant = SelectVariant.outlined
          label = Strings.location_access_type.get().toReactNode()

          LocationAccessType.entries.forEach { accessType ->
            MenuItem {
              value = accessType.name
              +accessType.localizedString.get()
            }
          }
        }
      }
    }

    spacer(16)

    TextField {
      placeholder = Strings.undefined.get()
      fullWidth = true
      variant = FormControlVariant.outlined
      type = InputType.number
      label = Strings.location_number_of_seats_hint.get().toReactNode()
      value = state.locationSeatCount?.toString() ?: ""
      onChange = { event ->
        val value = event.target.value
        setState {
          locationSeatCount = value.toIntOrNull()?.coerceIn(1, 10_000)
        }
      }
    }

    spacer(32)

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
}

fun ChildrenBuilder.renderAddLocation(config: AddLocationConfig) {
  AddLocation::class.react {
    this.config = config
  }
}