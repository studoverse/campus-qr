package views.guestCheckIn.guestCheckInOverview

import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.ActiveCheckIn
import com.studo.campusqr.common.payloads.CheckOutData
import kotlinx.browser.window
import kotlinx.js.jso
import mui.material.*
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.apiBase
import util.get
import views.guestCheckIn.locationIdWithSeat
import webcore.NetworkManager
import webcore.RComponent
import webcore.extensions.launch

class GuestCheckInRowConfig(
  val activeCheckIn: ActiveCheckIn,
  val onCheckedOut: () -> Unit,
  val onShowSnackbar: (String) -> Unit,
)

external interface GuestCheckInRowProps : Props {
  var config: GuestCheckInRowConfig
}

external interface GuestCheckInRowState : State

class GuestCheckInRow : RComponent<GuestCheckInRowProps, GuestCheckInRowState>() {
  override fun ChildrenBuilder.render() {
    TableRow {
      TableCell {
        +props.config.activeCheckIn.locationName
      }
      TableCell {
        +props.config.activeCheckIn.email
      }
      TableCell {
        +(props.config.activeCheckIn.seat?.toString() ?: "-")
      }
      TableCell {
        Button {
          variant = ButtonVariant.outlined
          color = ButtonColor.primary
          onClick = {
            val areYouSureText =
              Strings.guest_checkin_checkout_are_you_sure.get().format(props.config.activeCheckIn.email)
            if (window.confirm(areYouSureText)) {
              val locationId = with(props.config.activeCheckIn) { locationIdWithSeat(locationId, seat) }
              launch {
                val response = NetworkManager.post<String>(
                  "$apiBase/location/$locationId/checkout",
                  body = CheckOutData(email = props.config.activeCheckIn.email)
                )
                if (response == "ok") {
                  props.config.onCheckedOut()
                } else {
                  props.config.onShowSnackbar(Strings.error_try_again.get())
                }
              }
            }
          }
          +Strings.guest_checkin_check_out.get()
        }
      }
    }
  }
}

fun ChildrenBuilder.renderGuestCheckIntRow(handler: GuestCheckInRowProps.() -> Unit) {
  GuestCheckInRow::class.react {
    +jso(handler)
  }
}
