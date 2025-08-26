package views.locations.addLocation

import com.studo.campusqr.common.payloads.ClientLocation
import react.RefObject
import webcore.MbDialogRef

sealed class AddLocationConfig(val dialogRef: RefObject<MbDialogRef>, val onFinished: (response: String?) -> Unit) {
  class Create(dialogRef: RefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)

  class Edit(val location: ClientLocation, dialogRef: RefObject<MbDialogRef>, onFinished: (response: String?) -> Unit) :
    AddLocationConfig(dialogRef = dialogRef, onFinished = onFinished)
}