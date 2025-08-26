package views.users.listUsersToolbarView

import react.RefObject
import webcore.MbDialogRef

class ListUsersToolbarViewConfig(
  val dialogRef: RefObject<MbDialogRef>,
  val handleCreateOrAddUserResponse: (String?) -> Unit,
)