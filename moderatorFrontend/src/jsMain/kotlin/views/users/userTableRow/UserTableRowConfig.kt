package views.users.userTableRow

import com.studo.campusqr.common.payloads.ClientUser
import react.RefObject
import webcore.MbDialogRef

class UserTableRowConfig(
  val user: ClientUser,
  val dialogRef: RefObject<MbDialogRef>,
  val onEditFinished: (response: String?) -> Unit
)
