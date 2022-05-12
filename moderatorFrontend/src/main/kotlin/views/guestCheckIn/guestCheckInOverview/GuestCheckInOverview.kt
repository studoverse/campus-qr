package views.guestCheckIn.guestCheckInOverview

import app.AppContext
import app.appContextToInject
import com.studo.campusqr.common.payloads.ActiveCheckIn
import mui.material.*
import react.*
import util.Strings
import util.apiBase
import util.get
import views.common.*
import views.guestCheckIn.AddGuestCheckIn
import views.guestCheckIn.AddGuestCheckInConfig
import webcore.*
import webcore.extensions.launch

external interface GuestCheckinOverviewProps : Props

external interface GuestCheckInOverviewState : State {
  var activeGuestCheckIns: List<ActiveCheckIn>?
  var loadingCheckInList: Boolean
}

private class GuestCheckInOverview : RComponent<GuestCheckinOverviewProps, GuestCheckInOverviewState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(GuestCheckInOverview::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun GuestCheckInOverviewState.init() {
    activeGuestCheckIns = emptyList()
    loadingCheckInList = false
  }

  private fun fetchActiveGuestCheckIns() = launch {
    setState { loadingCheckInList }
    val response = NetworkManager.get<Array<ActiveCheckIn>>("$apiBase/report/listActiveGuestCheckIns")
    setState {
      if (response != null) {
        activeGuestCheckIns = response.toList()
      } else {
        appContext.showSnackbar(Strings.error_try_again.get())
      }
      loadingCheckInList = false
    }
  }

  override fun componentDidMount() {
    fetchActiveGuestCheckIns()
  }

  private fun renderAddGuestCheckInDialog() = appContext.showDialog(
    DialogConfig(
      title = Strings.guest_checkin_add_guest.get(),
      customContent = DialogConfig.CustomContent(AddGuestCheckIn::class) {
        config = AddGuestCheckInConfig(
          onGuestCheckedIn = {
            fetchActiveGuestCheckIns()
          },
        )
      },
    )
  )

  override fun ChildrenBuilder.render() {
    renderToolbarView(
      config = ToolbarViewConfig(
        title = Strings.guest_checkin.get(),
        buttons = listOf(
          ToolbarButton(
            text = Strings.guest_checkin_add_guest.get(),
            variant = ButtonVariant.contained,
            onClick = {
              renderAddGuestCheckInDialog()
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
                },
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
