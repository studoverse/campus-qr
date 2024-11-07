package views.users.listUsersToolbarView

import react.MutableRefObject
import webcore.MbDialogRef

class ListUsersToolbarViewConfig(
  val dialogRef: MutableRefObject<MbDialogRef>,
  val handleCreateOrAddUserResponse: (String?) -> Unit,
)