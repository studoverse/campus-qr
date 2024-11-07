package views.guestCheckIn.guestCheckInRow

import com.studo.campusqr.common.payloads.ActiveCheckIn

class GuestCheckInRowConfig(
  val activeCheckIn: ActiveCheckIn,
  val onCheckedOut: () -> Unit,
)