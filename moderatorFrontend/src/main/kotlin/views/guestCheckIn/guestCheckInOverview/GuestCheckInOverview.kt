package views.guestCheckIn.guestCheckInOverview

import com.studo.campusqr.common.payloads.ActiveCheckIn
import mui.material.*
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.apiBase
import util.get
import views.common.*
import views.guestCheckIn.AddGuestCheckInConfig
import views.guestCheckIn.renderAddGuestCheckIn
import webcore.*
import webcore.extensions.launch

external interface GuestCheckinOverviewProps : Props

external interface GuestCheckInOverviewState : State {
  var activeGuestCheckIns: List<ActiveCheckIn>?
  var showAddGuestCheckInDialog: Boolean
  var loadingCheckInList: Boolean
  var snackbarText: String
}

private class GuestCheckInOverview : RComponent<GuestCheckinOverviewProps, GuestCheckInOverviewState>() {

  override fun GuestCheckInOverviewState.init() {
    activeGuestCheckIns = emptyList()
    showAddGuestCheckInDialog = false
    loadingCheckInList = false
    snackbarText = ""
  }

  private fun fetchActiveGuestCheckIns() = launch {
    setState { loadingCheckInList }
    val response = NetworkManager.get<Array<ActiveCheckIn>>("$apiBase/report/listActiveGuestCheckIns")
    setState {
      if (response != null) {
        activeGuestCheckIns = response.toList()
      } else {
        snackbarText = Strings.error_try_again.get()
      }
      loadingCheckInList = false
    }
  }

  override fun componentDidMount() {
    fetchActiveGuestCheckIns()
  }

  private fun ChildrenBuilder.renderAddGuestCheckInDialog() = mbMaterialDialog(
    config = MbMaterialDialogConfig(
      show = state.showAddGuestCheckInDialog,
      title = Strings.guest_checkin_add_guest.get(),
      customContent = {
        renderAddGuestCheckIn(
          config = AddGuestCheckInConfig(
            onGuestCheckedIn = {
              setState { showAddGuestCheckInDialog = false }
              fetchActiveGuestCheckIns()
            },
            onShowSnackbar = { text ->
              setState {
                snackbarText = text
              }
            }
          )
        )
      },
      buttons = null,
      onClose = {
        setState {
          showAddGuestCheckInDialog = false
        }
      }
    )
  )

  private fun ChildrenBuilder.renderSnackbar() = mbSnackbar(
    config = MbSnackbarConfig(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      }
    )
  )

  override fun ChildrenBuilder.render() {
    renderSnackbar()
    renderAddGuestCheckInDialog()
    renderToolbarView(
      config = ToolbarViewConfig(
        title = Strings.guest_checkin.get(),
        buttons = listOf(
          ToolbarButton(
            text = Strings.guest_checkin_add_guest.get(),
            variant = ButtonVariant.contained,
            onClick = {
              setState {
                showAddGuestCheckInDialog = true
              }
            }
          )
        )
      )
    )
    renderMbLinearProgress(show = state.loadingCheckInList)

    when {
      state.activeGuestCheckIns?.isNotEmpty() == true -> Table {
        TableHead {
          TableRow {
            TableCell { +Strings.location_name.get() }
            TableCell { +Strings.email_address.get() }
            TableCell { +Strings.report_checkin_seat.get() }
            TableCell { }
          }
        }
        TableBody {
          state.activeGuestCheckIns!!.forEach { activeCheckIn ->
            renderGuestCheckInRow(
              config = GuestCheckInRowConfig(
                activeCheckIn,
                onCheckedOut = {
                  fetchActiveGuestCheckIns()
                  setState { snackbarText = Strings.guest_checkin_checkout_successful.get() }
                },
                onShowSnackbar = { text ->
                  setState { snackbarText = text }
                }
              )
            )
          }
        }
      }
      state.activeGuestCheckIns == null && !state.loadingCheckInList -> networkErrorView()
      !state.loadingCheckInList -> genericErrorView(
        title = Strings.guest_checkin_not_yet_added_title.get(),
        subtitle = Strings.guest_checkin_not_yet_added_subtitle.get()
      )
    }
  }
}

fun ChildrenBuilder.renderGuestCheckInOverview() {
  GuestCheckInOverview::class.react {}
}
