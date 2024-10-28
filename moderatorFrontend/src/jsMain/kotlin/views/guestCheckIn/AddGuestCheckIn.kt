package views.guestCheckIn

import app.GlobalCss
import web.cssom.*
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.CenteredProgressFc
import views.common.MbLinearProgressFc
import views.common.networkErrorView
import views.common.spacer
import webcore.*

class AddGuestCheckInConfig(
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onGuestCheckedIn: () -> Unit,
)

external interface AddGuestCheckInProps : Props {
  var config: AddGuestCheckInConfig
}

//@Lazy
val AddGuestCheckInFc = FcWithCoroutineScope<AddGuestCheckInProps> { props, launch ->
  val controller = AddGuestCheckInController.useAddGuestCheckInController(
    launch = launch,
    props = props,
  )

  fun ChildrenBuilder.renderSubmitButton() {
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
            if (controller.validateInput()) {
              controller.checkInGuest()
            }
          }
          +Strings.guest_checkin_add_guest.get()
        }
      }
    }
  }

  MbLinearProgressFc { show = controller.showProgress }

  if (!controller.locationFetchInProgress && controller.locationNameToLocationMap.isEmpty()) {
    networkErrorView()
    spacer(36)
  } else if (controller.locationFetchInProgress) {
    CenteredProgressFc {}
    spacer(36)
  } else {
    Autocomplete<AutocompleteProps<String>> {
      value = controller.selectedLocation?.name
      onChange = controller.locationAutoCompleteOnChange
      openOnFocus = true
      options = controller.locationNameToLocationMap.keys.toTypedArray()
      getOptionLabel = { it }
      renderInput = { params ->
        TextField.create {
          +params
          error = controller.selectedLocationTextFieldError.isNotEmpty()
          helperText = controller.selectedLocationTextFieldError.toReactNode()
          fullWidth = true
          variant = FormControlVariant.outlined
          label = Strings.location_name.get().toReactNode()
        }

      }
    }
    spacer(16)
    TextField {
      error = controller.personEmailTextFieldError.isNotEmpty()
      helperText = controller.personEmailTextFieldError.toReactNode()
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.email_address.get().toReactNode()
      value = controller.personEmailTextFieldValue
      onChange = controller.personEmailTextFieldOnChange
    }
    if (controller.selectedLocation?.seatCount != null) {
      val options = (1..controller.selectedLocation?.seatCount!!).map { it }.toTypedArray()
      spacer(16, key = "checkInSpacer1")
      Autocomplete<AutocompleteProps<Int>> {
        onChange = controller.seatInputAutocompleteOnChange
        fullWidth = true
        multiple = false
        openOnFocus = true
        this.options = options
        value = controller.seatInputValue
        getOptionLabel = { it.toString() }
        renderInput = { params ->
          TextField.create {
            +params
            error = controller.seatInputError.isNotEmpty()
            helperText = controller.seatInputError.toReactNode()
            variant = FormControlVariant.outlined
            label = Strings.report_checkin_seat.get().toReactNode()
          }
        }
      }
    }
    spacer(32, key = "checkInSpacer2")
    renderSubmitButton()
  }
}
