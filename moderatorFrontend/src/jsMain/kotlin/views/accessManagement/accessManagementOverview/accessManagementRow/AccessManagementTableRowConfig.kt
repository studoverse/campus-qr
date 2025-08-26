package views.accessManagement.accessManagementOverview.accessManagementRow

import com.studo.campusqr.common.payloads.ClientAccessManagement
import react.RefObject
import webcore.MbDialogRef

class AccessManagementTableRowConfig(
  val accessManagement: ClientAccessManagement,
  val dialogRef: RefObject<MbDialogRef>,
  val onOperationFinished: (operation: AccessManagementTableRowOperation, success: Boolean) -> Unit
)

enum class AccessManagementTableRowOperation {
  Edit, Delete, Duplicate
}
