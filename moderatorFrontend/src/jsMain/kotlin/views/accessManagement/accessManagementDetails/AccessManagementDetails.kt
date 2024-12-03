package views.accessManagement.accessManagementDetails

import mui.material.*
import react.*
import util.Strings
import util.get
import views.common.*
import webcore.*
import js.lazy.Lazy
import views.accessManagement.accessManagementDetails.accessManagementDetailsActionButtons.AccessManagementDetailsActionButtonsConfig
import views.accessManagement.accessManagementDetails.accessManagementDetailsActionButtons.AccessManagementDetailsActionButtons
import views.accessManagement.accessManagementDetails.accessManagementLocationSelection.AccessManagementLocationSelectionConfig
import views.accessManagement.accessManagementDetails.accessManagementLocationSelection.AccessManagementLocationSelection
import views.accessManagement.accessManagementDetails.permittedPeople.PermittedPeopleConfig
import views.accessManagement.accessManagementDetails.permittedPeople.PermittedPeople
import views.accessManagement.accessManagementDetails.timeSlotPicker.TimeSlotPickerConfig
import views.accessManagement.accessManagementDetails.timeSlotPicker.TimeSlotPicker

external interface AccessManagementDetailsProps : Props {
  var config: AccessManagementDetailsConfig
}

@Lazy
val AccessManagementDetails = FcWithCoroutineScope<AccessManagementDetailsProps> { props, launch ->
  val controller = AccessManagementDetailsController.use(config = props.config, launch = launch)

  fun ChildrenBuilder.renderNoteTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_note.get().toReactNode()
      value = controller.accessControlNoteTextFieldValue
      onChange = controller.noteTextFieldOnChange
    }
  }

  fun ChildrenBuilder.renderReasonTextField() {
    TextField {
      disabled = props.config is AccessManagementDetailsConfig.Details
      fullWidth = true
      variant = FormControlVariant.outlined
      label = Strings.access_control_reason.get().toReactNode()
      value = controller.accessControlReasonTextFieldValue
      onChange = controller.reasonTextFieldOnChange
    }
  }

  fun ChildrenBuilder.renderDetailsContent() {
    Suspense {
      AccessManagementLocationSelection {
        this.config = AccessManagementLocationSelectionConfig(
          isAutocompleteDisabled = props.config is AccessManagementDetailsConfig.Details,
          selectedLocation = controller.selectedLocation,
          selectedLocationTextFieldError = controller.selectedLocationTextFieldError,
          locationNameToLocationMap = controller.locationNameToLocationMap,
          locationSelectionOnChange = controller.locationSelectionOnChange,
        )
      }
    }
    spacer(16, key = "renderDetailsContentSpacer1")
    renderNoteTextField()
    spacer(16, key = "renderDetailsContentSpacer2")
    renderReasonTextField()
    spacer(24, key = "renderDetailsContentSpacer3")
    Suspense {
      TimeSlotPicker {
        this.config = TimeSlotPickerConfig(
          timeSlots = controller.timeSlots,
          fromDateTimeSlotErrors = controller.fromDateTimeSlotErrors,
          toDateTimeSlotErrors = controller.toDateTimeSlotErrors,
          accessManagementDetailsType = props.config,
          addTimeSlotOnClick = controller.addTimeSlotOnClick,
          removeTimeSlotOnClick = controller.removeTimeSlotOnClick,
          timeSlotDateFromOnChange = controller.timeSlotDateFromOnChange,
          timeSlotTimeFromOnChange = controller.timeSlotTimeFromOnChange,
          timeSlotDateToOnChange = controller.timeSlotDateToOnChange,
          timeSlotTimeToOnChange = controller.timeSlotTimeToOnChange,
        )
      }
    }
    Suspense {
      PermittedPeople {
        this.config = PermittedPeopleConfig(
          permittedPeopleList = controller.permittedPeopleList,
          personEmailTextFieldValue = controller.personEmailTextFieldValue,
          accessManagementDetailsType = props.config,
          submitPermittedPeopleToState = controller.submitPermittedPeopleToState,
          addPermittedPeopleOnChange = controller.addPermittedPeopleOnChange,
          removePermittedPeopleOnClick = controller.removePermittedPeopleOnClick,
        )
      }
    }
    spacer(32, key = "renderDetailsContentSpacer4")
    Suspense {
      AccessManagementDetailsActionButtons {
        this.config = AccessManagementDetailsActionButtonsConfig(
          accessManagementDetailsType = props.config,
          dialogRef = props.config.dialogRef,
          createAccessControlOnClick = {
            controller.createAccessControlOnClick(
              props.config,
            )
          },
        )
      }
    }
  }

  MbLinearProgress { show = controller.showProgress }

  if (!controller.locationFetchInProgress && controller.locationNameToLocationMap.isEmpty()) {
    networkErrorView()
    spacer(36)
  } else if (controller.locationFetchInProgress) {
    CenteredProgress {}
    spacer(36)
  } else {
    renderDetailsContent()
  }
}
