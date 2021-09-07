package views.guestCheckIn.guestCheckInOverview

import com.studo.campusqr.common.payloads.ActiveCheckIn
import react.*
import util.Strings
import util.apiBase
import util.get
import views.common.*
import views.guestCheckIn.AddGuestCheckInProps
import views.guestCheckIn.renderAddGuestCheckIn
import webcore.MbSnackbarProps
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbMaterialDialog
import webcore.mbSnackbar

interface GuestCheckinOverviewProps : RProps {
  var classes: GuestCheckInOverviewClasses
}

interface GuestCheckInOverviewState : RState {
  var activeGuestCheckIns: List<ActiveCheckIn>?
  var showAddGuestCheckInDialog: Boolean
  var loadingCheckInList: Boolean
  var snackbarText: String
}

class GuestCheckInOverview : RComponent<GuestCheckinOverviewProps, GuestCheckInOverviewState>() {

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

  private fun RBuilder.renderAddGuestCheckInDialog() = mbMaterialDialog(
    show = state.showAddGuestCheckInDialog,
    title = Strings.guest_checkin_add_guest.get(),
    customContent = {
      renderAddGuestCheckIn(
        AddGuestCheckInProps.Config(
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

  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarProps.Config(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      })
  )

  override fun RBuilder.render() {
    renderSnackbar()
    renderAddGuestCheckInDialog()
    renderToolbarView(ToolbarViewProps.Config(
      title = Strings.guest_checkin.get(),
      buttons = listOf(
        ToolbarViewProps.ToolbarButton(
          text = Strings.guest_checkin_add_guest.get(),
          variant = "contained",
          onClick = {
            setState {
              showAddGuestCheckInDialog = true
            }
          }
        )
      )
    ))
    renderLinearProgress(state.loadingCheckInList)

    when {
      state.activeGuestCheckIns?.isNotEmpty() == true -> mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.location_name.get() }
            mTableCell { +Strings.email_address.get() }
            mTableCell { +Strings.report_checkin_seat.get() }
            mTableCell { }
          }
        }
        mTableBody {
          state.activeGuestCheckIns!!.forEach { activeCheckIn ->
            renderGuestCheckIntRow(
              GuestCheckInRowProps.Config(
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
        Strings.guest_checkin_not_yet_added_title.get(),
        Strings.guest_checkin_not_yet_added_subtitle.get()
      )
    }
  }
}

interface GuestCheckInOverviewClasses

private val style = { _: dynamic ->
}

private val styled = withStyles<GuestCheckinOverviewProps, GuestCheckInOverview>(style)

fun RBuilder.renderGuestCheckInOverview() = styled {
  // Set component attrs here
}
  