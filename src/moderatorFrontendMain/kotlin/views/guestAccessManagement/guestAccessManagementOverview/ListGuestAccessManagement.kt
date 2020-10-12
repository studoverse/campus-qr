package views.guestAccessManagement.guestAccessManagementOverview

import apiBase
import app.GlobalCss
import app.routeContext
import com.studo.campusqr.common.ActiveCheckIn
import kotlinext.js.js
import react.*
import react.dom.div
import util.Strings
import util.get
import util.toRoute
import views.common.ToolbarView
import views.common.ToolbarViewProps
import views.common.renderLinearProgress
import views.common.renderToolbarView
import views.guestAccessManagement.renderGuestAccessManagementDetails
import webcore.MbSnackbarProps
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.muiButton
import webcore.materialUI.typography
import webcore.materialUI.withStyles
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
    val response = NetworkManager.get<Array<ActiveCheckIn>>("$apiBase/quest-access/list")
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
          onClick = { _ ->

          }
        )
      )
    ))
    renderLinearProgress(state.loadingAccessManagementList)
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
  