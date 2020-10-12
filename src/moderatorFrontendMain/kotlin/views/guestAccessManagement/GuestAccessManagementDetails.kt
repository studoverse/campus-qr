package views.guestAccessManagement

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import webcore.materialUI.withStyles

interface GuestAccessManagementDetailsProps : RProps {
  var classes: GuestAccessManagementDetailsClasses
}

interface GuestAccessManagementDetailsState : RState

class GuestAccessManagementDetails :
  RComponent<GuestAccessManagementDetailsProps, GuestAccessManagementDetailsState>() {
  override fun RBuilder.render() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}

interface GuestAccessManagementDetailsClasses {
  // Keep in sync with GuestAccessManagementDetailsStyle!
}

private val GuestAccessManagementDetailsStyle = { theme: dynamic ->
  // Keep in sync with GuestAccessManagementDetailsClasses!
}

private val styled = withStyles<GuestAccessManagementDetailsProps, GuestAccessManagementDetails>(GuestAccessManagementDetailsStyle)

fun RBuilder.renderGuestAccessManagementDetails() = styled {
  // Set component attrs here
}
  