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

val AddGuestCheckInFc = FcWithCoroutineScope<AddGuestCheckInProps> { props, launch ->
  val addGuestCheckInController = AddGuestCheckInController.useAddGuestCheckInController(
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
            if (addGuestCheckInController.validateInput()) {
              addGuestCheckInController.checkInGuest()
            }
          }
          +Strings.guest_checkin_add_guest.get()
        }
      }
    }
  }

  MbLinearProgressFc { show = addGuestCheckInController.showProgress }

  if (!addGuestCheckInController.locationFetchInProgress && addGuestCheckInController.locationNameToLocationMap.isEmpty()) {
    networkErrorView()
    spacer(36)
  } else if (addGuestCheckInController.locationFetchInProgress) {
    CenteredProgressFc {}
    spacer(36)
  } else {
    Autocomplete<AutocompleteProps<String>> {
      value = addGuestCheckInController.selectedLocation?.name
      onChange = addGuestCheckInController.locationAutoCompleteOnChange
      openOnFocus = true
      options = addGuestCheckInController.locationNameToLocationMap.keys.toTypedArray()
      getOptionLabel = { it }
      renderInput = { params ->
        TextField.create {
          +params
          error = addGuestCheckInController.selectedLocationTextFieldError.isNotEmpty()
          helperText = addGuestCheckInController.selectedLocationTextFieldError.toReactNode()
          fullWidth = true
          variant = FormControlVariant.outlined
          label = Strings.location_name.get().toReactNode()
        }

      }
    }
    spacer(16)
    TextField {
      error = addGuestCheckInController.personEmailTextFieldError.isNotEmpty()
      helperText = addGuestCheckInController.personEmailTextFieldError.toReactNode()
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.email_address.get().toReactNode()
      value = addGuestCheckInController.personEmailTextFieldValue
      onChange = addGuestCheckInController.personEmailTextFieldOnChange
    }
    if (addGuestCheckInController.selectedLocation?.seatCount != null) {
      val options = (1..addGuestCheckInController.selectedLocation?.seatCount!!).map { it }.toTypedArray()
      spacer(16)
      Autocomplete<AutocompleteProps<Int>> {
        // TODO: @mb
        onChange = { _, target: Any?, _, _ ->
          target as Int?
          addGuestCheckInController.seatInputAutoCompleteOnChange(target)
        }
        fullWidth = true
        multiple = false
        openOnFocus = true
        this.options = options
        value = addGuestCheckInController.seatInputValue
        getOptionLabel = { it.toString() }
        renderInput = { params ->
          TextField.create {
            +params
            error = addGuestCheckInController.seatInputError.isNotEmpty()
            helperText = addGuestCheckInController.seatInputError.toReactNode()
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

