package views.accessManagement.accessManagementDetails.accessManagementDetailsActionButtons

import react.RefObject
import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import webcore.MbDialogRef

data class AccessManagementDetailsActionButtonsConfig(
  val accessManagementDetailsType: AccessManagementDetailsConfig,
  val dialogRef: RefObject<MbDialogRef>,
  val createAccessControlOnClick: () -> Unit,
)