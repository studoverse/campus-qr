package views.locations.locationTableRow

import com.studo.campusqr.common.payloads.ClientLocation
import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.UserData
import react.MutableRefObject
import webcore.MbDialogRef

class LocationTableRowConfig(
  val location: ClientLocation,
  val onEditFinished: (response: String?) -> Unit,
  val onDeleteFinished: (response: String?) -> Unit,
  val userData: UserData,
  val dialogRef: MutableRefObject<MbDialogRef>,
) {
  val clientUser: ClientUser get() = userData.clientUser!!
}