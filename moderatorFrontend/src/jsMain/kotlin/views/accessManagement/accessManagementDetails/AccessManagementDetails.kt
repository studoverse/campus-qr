package views.accessManagement.accessManagementDetails

import com.studo.campusqr.common.payloads.*
import mui.material.*
import react.*
import util.Strings
import util.get
import views.common.*
import webcore.*
import js.lazy.Lazy

sealed class AccessManagementDetailsConfig(val dialogRef: RefObject<MbDialogRef>) {
  class Create(val locationId: String?, dialogRef: MutableRefObject<MbDialogRef>, val onCreated: () -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Edit(val accessManagement: ClientAccessManagement, dialogRef: MutableRefObject<MbDialogRef>, val onEdited: (Boolean) -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Details(val accessManagement: ClientAccessManagement, dialogRef: MutableRefObject<MbDialogRef>) :
    AccessManagementDetailsConfig(dialogRef)
}

class TimeSlotError(val text: String, val timeSlot: ClientDateRange)

external interface AccessManagementDetailsProps : Props {
  var config: AccessManagementDetailsConfig
}

//@Lazy
val AccessManagementDetailsFc = FcWithCoroutineScope<AccessManagementDetailsProps> { props, launch ->
  val controller = AccessManagementDetailsController.useAccessManagementDetailsController(config = props.config, launch = launch)

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
      AccessManagementLocationSelectionFc {
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
      TimeSlotPickerFc {
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
      PermittedPeopleFc {
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
      AccessManagementDetailsActionButtonsFc {
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

  MbLinearProgressFc { show = controller.showProgress }

  if (!controller.locationFetchInProgress && controller.locationNameToLocationMap.isEmpty()) {
    networkErrorView()
    spacer(36)
  } else if (controller.locationFetchInProgress) {
    CenteredProgressFc {}
    spacer(36)
  } else {
    renderDetailsContent()
  }
}
