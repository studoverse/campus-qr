package views.locations

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.ClientLocation
import com.studo.campusqr.common.LocationAccessType
import com.studo.campusqr.common.accessTypeEnum
import com.studo.campusqr.common.extensions.format
import kotlinext.js.js
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import util.Strings
import util.get
import util.localizedString
import views.common.spacer
import views.locations.AddLocationProps.Config
import webcore.NetworkManager
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.materialUI.*
import kotlin.js.json

interface AddLocationProps : RProps {
  sealed class Config(val onFinished: (response: String?) -> Unit) {
    class Create(onFinished: (response: String?) -> Unit) : Config(onFinished)
    class Edit(val location: ClientLocation, onFinished: (response: String?) -> Unit) : Config(onFinished)
  }

  var config: Config
  var classes: AddLocationClasses
}

interface AddLocationState : RState {
  var locationCreationInProgress: Boolean
  var locationTextFieldValue: String
  var locationTextFieldError: String
  var accessType: LocationAccessType
}

class AddLocation(props: AddLocationProps) : RComponent<AddLocationProps, AddLocationState>(props) {

  override fun AddLocationState.init(props: AddLocationProps) {
    locationCreationInProgress = false
    locationTextFieldError = ""
    locationTextFieldValue = (props.config as? Config.Edit)?.location?.name ?: ""
    accessType = (props.config as? Config.Edit)?.location?.accessTypeEnum ?: LocationAccessType.FREE
  }

  private fun createNewLocation() = launch {
    setState { locationCreationInProgress = true }
    val response = NetworkManager.post<String>(
      url = "$apiBase/location/create",
      params = json(
        "name" to state.locationTextFieldValue,
        "accessType" to state.accessType.name
      )
    )
    setState {
      locationCreationInProgress = false
    }
    props.config.onFinished(response)
  }

  private fun editLocation() = launch {
    setState { locationCreationInProgress = true }
    val locationId = (props.config as Config.Edit).location.id
    val response = NetworkManager.post<String>(
      url = "$apiBase/location/$locationId/edit",
      params = json(
        "name" to state.locationTextFieldValue,
        "accessType" to state.accessType.name
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

  override fun RBuilder.render() {
    textField {
      attrs.error = state.locationTextFieldError.isNotEmpty()
      attrs.helperText = state.locationTextFieldError
      attrs.fullWidth = true
      attrs.variant = "outlined"
      attrs.label = Strings.location_name.get()
      attrs.value = state.locationTextFieldValue
      attrs.inputProps = js {
        maxLength = 40 // Make sure names stay printable
      }
      attrs.onChange = { event: Event ->
        val value = event.inputValue
        setState {
          locationTextFieldValue = value
          locationTextFieldError = ""
        }
      }
    }

    spacer(16)

    div(classes = props.classes.accessTypeSwitch) {
      formControl {
        attrs.fullWidth = true
        inputLabel {
          +Strings.user_permission.get()
        }
        attrs.variant = "outlined"
        muiSelect {
          attrs.value = state.accessType.toString()
          attrs.onChange = { event ->
            val value = event.target.value as String
            setState {
              accessType = LocationAccessType.valueOf(value)
            }
          }
          attrs.variant = "outlined"
          attrs.label = Strings.location_access_type.get()

          LocationAccessType.values().forEach { accessType ->
            menuItem {
              attrs.value = accessType.toString()
              +accessType.localizedString.get()
            }
          }
        }
      }
    }

    spacer(32)

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
                is Config.Edit -> editLocation()
                is Config.Create -> createNewLocation()
              }
            }
          }
          +if (props.config is Config.Create) {
            Strings.location_add.get()
          } else {
            Strings.location_update.get()
          }
        }
      }
    }
  }
}

interface AddLocationClasses {
  // Keep in sync with AddLocationStyle!
  var addButton: String
  var accessTypeSwitch: String
}

private val AddLocationStyle = { theme: dynamic ->
  // Keep in sync with AddLocationClasses!
  js {
    addButton = js {
      marginBottom = 16
    }
    accessTypeSwitch = js {
      display = "flex"
      justifyContent = "center"
      alignItems = "center"
      fontFamily = "'Roboto', Arial, sans-serif"
    }
  }
}

private val styled = withStyles<AddLocationProps, AddLocation>(AddLocationStyle)

fun RBuilder.renderAddLocation(config: Config) = styled {
  attrs.config = config
}
  