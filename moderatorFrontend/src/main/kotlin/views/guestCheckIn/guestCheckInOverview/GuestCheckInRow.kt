package views.guestCheckIn.guestCheckInOverview

import app.AppContext
import app.appContextToInject
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.ActiveCheckIn
import com.studo.campusqr.common.payloads.CheckOutData
import kotlinx.browser.window
import mui.material.*
import react.*
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
)

external interface GuestCheckInRowProps : Props {
  var config: GuestCheckInRowConfig
}

external interface GuestCheckInRowState : State

private class GuestCheckInRow : RComponent<GuestCheckInRowProps, GuestCheckInRowState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(GuestCheckInRow::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

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
                  appContext.showSnackbar(Strings.guest_checkin_checkout_successful.get())
                } else {
                  appContext.showSnackbar(Strings.error_try_again.get())
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

fun ChildrenBuilder.renderGuestCheckInRow(config: GuestCheckInRowConfig) {
  GuestCheckInRow::class.react {
    this.config = config
  }
}
