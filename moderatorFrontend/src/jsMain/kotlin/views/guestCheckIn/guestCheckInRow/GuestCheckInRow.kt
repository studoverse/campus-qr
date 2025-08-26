package views.guestCheckIn.guestCheckInRow

import app.appContextToInject
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.CheckOutData
import js.lazy.Lazy
import mui.material.*
import react.*
import util.Strings
import util.apiBase
import util.get
import views.guestCheckIn.guestCheckInOverview.GuestCheckInOverviewController.Companion.locationIdWithSeat
import web.prompts.confirm
import webcore.FcWithCoroutineScope
import webcore.NetworkManager

external interface GuestCheckInRowProps : Props {
  var config: GuestCheckInRowConfig
}

@Lazy
val GuestCheckInRow = FcWithCoroutineScope<GuestCheckInRowProps> { props, launch ->
  val appContext = use(appContextToInject)!!

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
          if (confirm(areYouSureText)) {
            val locationId = with(props.config.activeCheckIn) { locationIdWithSeat(locationId, seat) }
            launch {
              val response = NetworkManager.post<String>(
                "$apiBase/location/$locationId/checkout",
                body = CheckOutData(email = props.config.activeCheckIn.email)
              )
              if (response == "ok") {
                props.config.onCheckedOut()
                appContext.showSnackbarText(Strings.guest_checkin_checkout_successful.get())
              } else {
                appContext.showSnackbarText(Strings.error_try_again.get())
              }
            }
          }
        }
        +Strings.guest_checkin_check_out.get()
      }
    }
  }
}