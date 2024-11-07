package views.accessManagement.accessManagementOverview.accessManagementRow

import com.studo.campusqr.common.payloads.ClientAccessManagement
import react.MutableRefObject
import webcore.MbDialogRef

class AccessManagementTableRowConfig(
  val accessManagement: ClientAccessManagement,
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onOperationFinished: (operation: AccessManagementTableRowOperation, success: Boolean) -> Unit
)

enum class AccessManagementTableRowOperation {
  Edit, Delete, Duplicate
}
