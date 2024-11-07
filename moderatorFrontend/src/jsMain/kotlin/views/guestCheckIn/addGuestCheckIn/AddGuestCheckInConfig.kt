package views.guestCheckIn.addGuestCheckIn

import react.MutableRefObject
import webcore.MbDialogRef

class AddGuestCheckInConfig(
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onGuestCheckedIn: () -> Unit,
)