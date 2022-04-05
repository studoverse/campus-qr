package views.locations

import app.GlobalCss
import com.studo.campusqr.common.LocationAccessType
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.CreateOrUpdateLocationData
import csstype.*
import kotlinx.js.jso
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.InputType
import util.Strings
import util.apiBase
import util.get
import util.localizedString
import views.common.spacer
import webcore.*
import webcore.extensions.launch

sealed class AddLocationConfig(val onFinished: (response: String?) -> Unit) {
  class Create(onFinished: (response: String?) -> Unit) : AddLocationConfig(onFinished)
  class Edit(val location: ClientLocation, onFinished: (response: String?) -> Unit) : AddLocationConfig(onFinished)
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

@Suppress("UPPER_BOUND_VIOLATED")
private class AddLocation(props: AddLocationProps) : RComponent<AddLocationProps, AddLocationState>(props) {

  override fun AddLocationState.init(props: AddLocationProps) {
    locationCreationInProgress = false
    locationTextFieldError = ""
    locationTextFieldValue = (props.config as? AddLocationConfig.Edit)?.location?.name ?: ""
    locationAccessType = (props.config as? AddLocationConfig.Edit)?.location?.accessType ?: LocationAccessType.FREE
    locationSeatCount = (props.config as? AddLocationConfig.Edit)?.location?.seatCount
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
    TextField<OutlinedTextFieldProps> {
      error = state.locationTextFieldError.isNotEmpty()
      helperText = ReactNode(state.locationTextFieldError)
      fullWidth = true
      variant = FormControlVariant.outlined()
      label = ReactNode(Strings.location_name.get())
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
          // TODO: @mh See: https://youtrack.jetbrains.com/issue/KT-51698
          this as ChildrenBuilder
          value = state.locationAccessType.toString()
          // TODO: @mh See: https://github.com/JetBrains/kotlin-wrappers/issues/1356
          asDynamic().onChange = { event: dynamic ->
            setState {
              locationAccessType = LocationAccessType.valueOf(event.target.value)
            }
          }
          variant = SelectVariant.outlined
          label = ReactNode(Strings.location_access_type.get())

          LocationAccessType.values().forEach { accessType ->
            MenuItem {
              value = accessType.toString()
              +accessType.localizedString.get()
            }
          }
        }
      }
    }

    spacer(16)

    TextField<OutlinedTextFieldProps> {
      placeholder = Strings.undefined.get()
      fullWidth = true
      variant = FormControlVariant.outlined()
      type = InputType.number
      label = ReactNode(Strings.location_number_of_seats_hint.get())
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

fun ChildrenBuilder.renderAddLocation(handler: AddLocationProps.() -> Unit) {
  AddLocation::class.react {
    +jso(handler)
  }
}