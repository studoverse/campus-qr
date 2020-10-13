package views.guestAccessManagement.guestAccessManagementOverview

import apiBase
import com.studo.campusqr.common.ActiveCheckIn
import com.studo.campusqr.common.extensions.format
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import util.Strings
import util.get
import views.guestAccessManagement.locationIdWithSeat
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.mTableCell
import webcore.materialUI.mTableRow
import webcore.materialUI.muiButton
import webcore.materialUI.withStyles
import kotlin.js.json

interface GuestAccessManagementRowProps : RProps {
  var classes: QuestAccessManagementRowClasses
  var config: Config
  class Config(
    val activeCheckIn: ActiveCheckIn,
    val onCheckedOut: () -> Unit,
    val onShowSnackbar: (String) -> Unit,
  )
}

interface GuestAccessManagementRowState : RState

class GuestAccessManagementRow : RComponent<GuestAccessManagementRowProps, GuestAccessManagementRowState>() {
  override fun RBuilder.render() {
    mTableRow {
      mTableCell {
        +props.config.activeCheckIn.locationName
      }
      mTableCell {
        +props.config.activeCheckIn.email
      }
      mTableCell {
        +(props.config.activeCheckIn.seat?.toString() ?: "-")
      }
      mTableCell {
        muiButton {
          attrs.variant = "outlined"
          attrs.color = "primary"
          attrs.onClick = {
            val areYouSureText =
              Strings.guest_access_control_checkout_are_you_sure.get().format(props.config.activeCheckIn.email)
            if (window.confirm(areYouSureText)) {
              val locationId = with(props.config.activeCheckIn) { locationIdWithSeat(locationId, seat) }
              launch {
                val response = NetworkManager.post<String>(
                  "$apiBase/location/$locationId/checkout", params = json(
                    "email" to props.config.activeCheckIn.email
                  )
                )
                if (response == "ok") {
                  props.config.onCheckedOut()
                } else {
                  props.config.onShowSnackbar(Strings.error_try_again.get())
                }
              }
            }
          }
          +Strings.guest_access_control_check_out.get()
        }
      }
    }
  }
}

interface QuestAccessManagementRowClasses {
  // Keep in sync with QuestAccessManagementRowStyle!
}

private val QuestAccessManagementRowStyle = { theme: dynamic ->
  // Keep in sync with QuestAccessManagementRowClasses!
}

private val styled = withStyles<GuestAccessManagementRowProps, GuestAccessManagementRow>(QuestAccessManagementRowStyle)

fun RBuilder.renderGuestAccessManagementRow(config: GuestAccessManagementRowProps.Config) = styled {
  attrs.config = config
}
  