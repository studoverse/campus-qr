package views.users.userTableRow

import com.studo.campusqr.common.payloads.ClientUser
import react.MutableRefObject
import webcore.MbDialogRef

class UserTableRowConfig(
  val user: ClientUser,
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onEditFinished: (response: String?) -> Unit
)
