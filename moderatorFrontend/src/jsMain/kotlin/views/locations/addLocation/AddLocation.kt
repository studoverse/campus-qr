package views.locations.addLocation

import web.cssom.*
import app.GlobalCss
import com.studo.campusqr.common.LocationAccessType
import js.lazy.Lazy
import js.objects.unsafeJso
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.get
import util.localizedString
import views.common.MbLinearProgress
import views.common.spacer
import web.html.InputType
import web.html.number
import webcore.*

external interface AddLocationProps : Props {
  var config: AddLocationConfig
}

@Lazy
val AddLocation = FcWithCoroutineScope<AddLocationProps> { props, launch ->
  val controller = AddLocationController.use(config = props.config, launch = launch)

  MbLinearProgress { show = controller.locationCreationInProgress }

  TextField {
    error = controller.locationTextFieldError.isNotEmpty()
    helperText = controller.locationTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    label = Strings.location_name.get().toReactNode()
    value = controller.locationTextFieldValue
    inputProps = unsafeJso {
      maxLength = 40 // Make sure names stay printable
    }
    onChange = controller.locationNameOnChange
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
        value = controller.locationAccessType.name
        onChange = controller.locationAccessTypeOnChange
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
    value = controller.locationSeatCount?.toString() ?: ""
    onChange = controller.locationSeatCountOnChange
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
        onClick = controller.createOrEditLocationIfInputValid
        +if (props.config is AddLocationConfig.Create) {
          Strings.location_add.get()
        } else {
          Strings.location_update.get()
        }
      }
    }
  }
}
