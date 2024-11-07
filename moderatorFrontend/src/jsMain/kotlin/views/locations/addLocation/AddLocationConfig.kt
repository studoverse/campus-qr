package views.locations.addLocation

import com.studo.campusqr.common.payloads.ClientLocation
import react.MutableRefObject
import webcore.MbDialogRef

sealed class AddLocationConfig(val dialogRef: MutableRefObject<MbDialogRef>, val onFinished: (response: String?) -> Unit) {
  class Create(dialogRef: MutableRefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)

  class Edit(val location: ClientLocation, dialogRef: MutableRefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)
}