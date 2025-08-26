package views.guestCheckIn.addGuestCheckIn

import react.RefObject
import webcore.MbDialogRef

class AddGuestCheckInConfig(
  val dialogRef: RefObject<MbDialogRef>,
  val onGuestCheckedIn: () -> Unit,
)