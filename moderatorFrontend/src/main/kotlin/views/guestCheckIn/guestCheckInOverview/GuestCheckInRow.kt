package views.guestCheckIn.guestCheckInOverview

import com.studo.campusqr.common.ActiveCheckIn
import com.studo.campusqr.common.extensions.format
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import util.Strings
import util.apiBase
import util.get
import views.guestCheckIn.locationIdWithSeat
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.mTableCell
import webcore.materialUI.mTableRow
import webcore.materialUI.muiButton
import webcore.materialUI.withStyles
import kotlin.js.json

interface GuestCheckInRowProps : RProps {
  var classes: GuestCheckInRowClasses
  var config: Config

  class Config(
    val activeCheckIn: ActiveCheckIn,
    val onCheckedOut: () -> Unit,
    val onShowSnackbar: (String) -> Unit,
  )
}

interface GuestCheckInRowState : RState

class GuestCheckInRow : RComponent<GuestCheckInRowProps, GuestCheckInRowState>() {
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
              Strings.guest_checkin_checkout_are_you_sure.get().format(props.config.activeCheckIn.email)
            if (window.confirm(areYouSureText)) {
              val locationId = with(props.config.activeCheckIn) { locationIdWithSeat(locationId, seat) }
              launch {
                val response = NetworkManager.post<String>(
                  "$apiBase/location/$locationId/checkout",
                  urlParams = mapOf("email" to props.config.activeCheckIn.email)
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

interface GuestCheckInRowClasses

private val GuestCheckInRowStyle = { _: dynamic ->
}

private val styled = withStyles<GuestCheckInRowProps, GuestCheckInRow>(GuestCheckInRowStyle)

fun RBuilder.renderGuestCheckIntRow(config: GuestCheckInRowProps.Config) = styled {
  attrs.config = config
}
  