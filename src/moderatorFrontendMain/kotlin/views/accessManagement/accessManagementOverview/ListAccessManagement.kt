package views.accessManagement.accessManagementOverview

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.ClientAccessManagement
import kotlinext.js.js
import react.*
import react.dom.div
import util.Strings
import util.get
import views.accessManagement.AccessManagementDetailsProps
import views.accessManagement.renderAccessManagementDetails
import views.common.genericErrorView
import views.common.networkErrorView
import webcore.MbSnackbarProps
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbMaterialDialog
import webcore.mbSnackbar

interface ListAccessManagementProps : RProps {
  var classes: ListLocationsClasses
}

interface ListAccessManagementState : RState {
  var accessManagementList: List<ClientAccessManagement>?
  var showAddAccessManagementDialog: Boolean
  var showAccessManagementImportDialog: Boolean
  var loadingAccessManagementList: Boolean
  var snackbarText: String
}

class ListAccessManagement : RComponent<ListAccessManagementProps, ListAccessManagementState>() {

  override fun ListAccessManagementState.init() {
    accessManagementList = null
    showAddAccessManagementDialog = false
    showAccessManagementImportDialog = false
    loadingAccessManagementList = false
    snackbarText = ""
  }

  private fun fetchAccessManagementList() = launch {
    setState { loadingAccessManagementList = true }
    val response = NetworkManager.get<Array<ClientAccessManagement>>("$apiBase/access/list")
    setState {
      accessManagementList = response?.toList()
      loadingAccessManagementList = false
    }
  }

  override fun componentDidMount() {
    fetchAccessManagementList()
  }

  private fun handleCreateOrEditLocationResponse(response: String?) {
    setState {
      snackbarText = when (response) {
        "ok" -> {
          fetchAccessManagementList()
          showAddAccessManagementDialog = false
          Strings.location_created.get()
        }
        else -> Strings.error_try_again.get()
      }
    }
  }

  private fun RBuilder.renderAddAccessManagementDialog() = mbMaterialDialog(
      show = state.showAddAccessManagementDialog,
      title = Strings.access_control_create.get(),
      customContent = {
        renderAccessManagementDetails(AccessManagementDetailsProps.Config.Create(onCreated = { success ->
          fetchAccessManagementList()
        }))
      },
      buttons = null,
      onClose = {
        setState {
          showAddAccessManagementDialog = false
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
    renderAddAccessManagementDialog()
    renderSnackbar()

    div(GlobalCss.flex) {
      typography {
        attrs.className = props.classes.header
        attrs.variant = "h5"
        +Strings.access_control.get()
      }
      div(GlobalCss.flexEnd) {

        muiButton {
          attrs.classes = js {
            root = props.classes.createButton
          }
          attrs.variant = "contained"
          attrs.color = "primary"
          attrs.onClick = {
            setState {
              showAddAccessManagementDialog = true
            }
          }
          +Strings.access_control_create.get()
        }
      }
    }

    div(props.classes.progressHolder) {
      if (state.loadingAccessManagementList) {
        linearProgress {}
      }
    }

    if (state.accessManagementList?.isNotEmpty() == true) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.location_name.get() }
            mTableCell { +Strings.access_control_permitted_people.get() }
            mTableCell { +Strings.access_control_note.get() }
            mTableCell { +Strings.actions.get() }
          }
        }
        mTableBody {

          state.accessManagementList!!.forEach { accessManagement ->
            renderAccessManagementRow(
              AccessManagementTableRowProps.Config(accessManagement, onEditFinished = { response ->
                handleCreateOrEditLocationResponse(response)
              })
            )
          }
        }
      }
    } else if (state.accessManagementList == null && !state.loadingAccessManagementList) {
      networkErrorView()
    } else if (!state.loadingAccessManagementList) {
      genericErrorView(
        Strings.access_control_not_configured_yet.get(),
        Strings.access_control_not_configured_yet_subtitle.get()
      )
    }
  }
}

interface ListLocationsClasses {
  var header: String
  var button: String
  var createButton: String
  var progressHolder: String
  // Keep in sync with ListLocationsStyle!
}

private val ListLocationsStyle = { theme: dynamic ->
  // Keep in sync with ListLocationsClasses!
  js {
    header = js {
      margin = 16
    }
    button = js {
      marginRight = 16
      marginTop = 16
      marginBottom = 16
      marginLeft = 8
    }
    createButton = js {
      margin = 16
    }
    progressHolder = js {
      height = 8
    }
  }
}

private val styled = withStyles<ListAccessManagementProps, ListAccessManagement>(ListLocationsStyle)

fun RBuilder.renderAccessManagementList() = styled {
  // Set component attrs here
}
