package views.guestAccessManagement.guestAccessManagementOverview

import apiBase
import com.studo.campusqr.common.ActiveCheckIn
import react.*
import util.Strings
import util.get
import views.common.*
import views.guestAccessManagement.GuestAccessManagementDetailsProps
import views.guestAccessManagement.renderGuestAccessManagementDetails
import webcore.MbSnackbarProps
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbMaterialDialog
import webcore.mbSnackbar

interface ListGuestAccessManagementProps : RProps {
  var classes: ListGuestAccessManagementClasses
}

interface ListGuestAccessManagementState : RState {
  var activeGuestCheckIns: List<ActiveCheckIn>?
  var showAddGuestCheckInDialog: Boolean
  var loadingAccessManagementList: Boolean
  var snackbarText: String
}

class ListGuestAccessManagement : RComponent<ListGuestAccessManagementProps, ListGuestAccessManagementState>() {

  override fun ListGuestAccessManagementState.init() {
    activeGuestCheckIns = emptyList()
    showAddGuestCheckInDialog = false
    loadingAccessManagementList = false
    snackbarText = ""
  }

  private fun fetchActiveGuestCheckIns() = launch {
    setState { loadingAccessManagementList }
    val response = NetworkManager.get<Array<ActiveCheckIn>>("$apiBase/report/listGuestCheckIns")
    setState {
      if (response != null) {
        activeGuestCheckIns = response.toList()
      } else {
        snackbarText = Strings.error_try_again.get()
      }
      loadingAccessManagementList = false
    }
  }

  override fun componentDidMount() {
    fetchActiveGuestCheckIns()
  }

  private fun RBuilder.renderAddGuestAccessManagementDialog() = mbMaterialDialog(
    show = state.showAddGuestCheckInDialog,
    title = Strings.access_control_create.get(),
    customContent = {
      renderGuestAccessManagementDetails(
        GuestAccessManagementDetailsProps.Config(
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
    renderAddGuestAccessManagementDialog()
    renderToolbarView(ToolbarViewProps.Config(
      title = "Guest Access Control",
      buttons = listOf(
        ToolbarViewProps.ToolbarButton(
          text = "Add guest",
          variant = "contained",
          onClick = {
            setState {
              showAddGuestCheckInDialog = true
            }
          }
        )
      )
    ))
    renderLinearProgress(state.loadingAccessManagementList)

    when {
      state.activeGuestCheckIns?.isNotEmpty() == true -> mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.location_name.get() }
            mTableCell { +Strings.email_address.get() }
            mTableCell { +Strings.report_checkin_seat.get() }
            mTableCell {  }
          }
        }
        mTableBody {
          state.activeGuestCheckIns!!.forEach { activeCheckIn ->
            renderGuestAccessManagementRow(
              GuestAccessManagementRowProps.Config(
                activeCheckIn,
                onCheckedOut = {
                  fetchActiveGuestCheckIns()
                  setState { snackbarText = "Checkout successful" }
                },
                onShowSnackbar = { text ->
                  setState { snackbarText = text }
                }
              )
            )
          }
        }
      }
      state.activeGuestCheckIns == null && !state.loadingAccessManagementList -> networkErrorView()
      !state.loadingAccessManagementList -> genericErrorView(
        Strings.guest_access_control_not_yet_added_title.get(),
        Strings.guest_access_control_not_yet_added_subtitle.get()
      )
    }
  }
}

interface ListGuestAccessManagementClasses {
  // Keep in sync with ListGuestAccessManagementStyle!
}

private val ListGuestAccessManagementStyle = { theme: dynamic ->
  // Keep in sync with ListGuestAccessManagementClasses!
}

private val styled = withStyles<ListGuestAccessManagementProps, ListGuestAccessManagement>(
  ListGuestAccessManagementStyle
)

fun RBuilder.renderListGuestAccessManagement() = styled {
  // Set component attrs here
}
  