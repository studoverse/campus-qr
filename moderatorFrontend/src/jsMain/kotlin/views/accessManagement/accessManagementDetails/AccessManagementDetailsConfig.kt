package views.accessManagement.accessManagementDetails

import com.studo.campusqr.common.payloads.ClientAccessManagement
import react.RefObject
import webcore.MbDialogRef

sealed class AccessManagementDetailsConfig(val dialogRef: RefObject<MbDialogRef>) {
  class Create(val locationId: String?, dialogRef: RefObject<MbDialogRef>, val onCreated: () -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Edit(val accessManagement: ClientAccessManagement, dialogRef: RefObject<MbDialogRef>, val onEdited: (Boolean) -> Unit) :
    AccessManagementDetailsConfig(dialogRef)

  class Details(val accessManagement: ClientAccessManagement, dialogRef: RefObject<MbDialogRef>) :
    AccessManagementDetailsConfig(dialogRef)
}
