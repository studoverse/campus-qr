package views.locations.locationTableRow

import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.UserData
import react.RefObject
import webcore.MbDialogRef

class LocationTableRowConfig(
  val location: ClientLocation,
  val onEditFinished: (response: String?) -> Unit,
  val onDeleteFinished: (response: String?) -> Unit,
  val userData: UserData,
  val dialogRef: RefObject<MbDialogRef>,
) {
  val clientUser: ClientUser get() = userData.clientUser!!
}