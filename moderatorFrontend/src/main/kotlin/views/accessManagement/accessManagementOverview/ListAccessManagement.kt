package views.accessManagement.accessManagementOverview

import com.studo.campusqr.common.payloads.AccessManagementData
import com.studo.campusqr.common.payloads.ClientAccessManagement
import com.studo.campusqr.common.payloads.ClientLocation
import kotlinx.js.jso
import mui.material.*
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.*
import views.accessManagement.AccessManagementDetailsConfig
import views.accessManagement.renderAccessManagementDetails
import views.common.*
import webcore.*
import webcore.extensions.launch

external interface ListAccessManagementProps : Props {
  var locationId: String?
}

external interface ListAccessManagementState : State {
  var accessManagementList: List<ClientAccessManagement>?
  var clientLocation: ClientLocation?
  var showAddAccessManagementDialog: Boolean
  var showAccessManagementImportDialog: Boolean
  var loadingAccessManagementList: Boolean
  var snackbarText: String
}

class ListAccessManagement : RComponent<ListAccessManagementProps, ListAccessManagementState>() {

  override fun ListAccessManagementState.init() {
    accessManagementList = null
    clientLocation = null
    showAddAccessManagementDialog = false
    showAccessManagementImportDialog = false
    loadingAccessManagementList = false
    snackbarText = ""
  }

  private fun fetchAccessManagementList() = launch {
    setState { loadingAccessManagementList = true }
    val params = props.locationId?.let { "?locationId=$it" } ?: ""
    val response = NetworkManager.get<AccessManagementData>("$apiBase/access/list$params")
    setState {
      accessManagementList = response?.accessManagement?.toList()
      clientLocation = response?.clientLocation
      loadingAccessManagementList = false
    }
  }

  override fun componentDidUpdate(
    prevProps: ListAccessManagementProps, prevState: ListAccessManagementState,
    snapshot: Any
  ) {
    if (prevProps.locationId != props.locationId) {
      setState {
        init()
      }
      fetchAccessManagementList()
    }
  }

  override fun componentDidMount() {
    fetchAccessManagementList()
  }

  private fun ChildrenBuilder.renderAddAccessManagementDialog() = mbMaterialDialog(handler = {
    config = MbMaterialDialogConfig(
      show = state.showAddAccessManagementDialog,
      title = Strings.access_control_create.get(),
      customContent = {
        renderAccessManagementDetails {
          config = AccessManagementDetailsConfig.Create(
            locationId = props.locationId,
            onCreated = { success ->
              setState {
                showAddAccessManagementDialog = false
                snackbarText = if (success) {
                  Strings.access_control_created_successfully.get()
                } else {
                  Strings.error_try_again.get()
                }
              }
              fetchAccessManagementList()
            })
        }
      },
      buttons = null,
      onClose = {
        setState {
          showAddAccessManagementDialog = false
        }
      }
    )
  })

  private fun ChildrenBuilder.renderSnackbar() = mbSnackbar {
    config = MbSnackbarConfig(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      })
  }

  override fun ChildrenBuilder.render() {
    renderAddAccessManagementDialog()
    renderSnackbar()
    renderToolbarView {
      config = ToolbarViewConfig(
        title = StringBuilder().apply {
          append(Strings.access_control.get())
          append(" - ")
          if (state.clientLocation == null) {
            append(Strings.access_control_my.get())
          } else {
            append(state.clientLocation!!.name)
          }
        }.toString(),
        buttons = listOf(
          ToolbarButton(
            text = Strings.access_control_export.get(),
            variant = ButtonVariant.outlined,
            onClick = { routeContext ->
              if (props.locationId == null) {
                routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LIST_EXPORT.toRoute()!!)
              } else {
                routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT.toRoute(pathParams = mapOf("id" to props.locationId!!))!!)
              }
            }
          ),
          ToolbarButton(
            text = Strings.access_control_create.get(),
            variant = ButtonVariant.contained,
            onClick = { _ ->
              setState {
                showAddAccessManagementDialog = true
              }
            }
          )
        )
      )
    }

    renderMbLinearProgress { show = state.loadingAccessManagementList }

    when {
      state.accessManagementList?.isNotEmpty() == true -> Table {
        TableHead {
          TableRow {
            TableCell { +Strings.location_name.get() }
            TableCell { +Strings.access_control_time_slots.get() }
            TableCell { +Strings.access_control_permitted_people.get() }
            TableCell { +Strings.access_control_note.get() }
            TableCell { +Strings.actions.get() }
          }
        }
        TableBody {
          state.accessManagementList!!.forEach { accessManagement ->
            renderAccessManagementRow {
              config = AccessManagementTableRowConfig(accessManagement,
                onOperationFinished = { operation, success ->
                  setState {
                    snackbarText = if (success) {
                      fetchAccessManagementList()
                      when (operation) {
                        AccessManagementTableRowOperation.Edit -> Strings.access_control_edited_successfully.get()
                        AccessManagementTableRowOperation.Duplicate -> Strings.access_control_duplicated_successfully.get()
                        AccessManagementTableRowOperation.Delete -> Strings.access_control_deleted_successfully.get()
                      }
                    } else {
                      Strings.error_try_again.get()
                    }
                  }
                }
              )
            }
          }
        }
      }
      state.accessManagementList == null && !state.loadingAccessManagementList -> networkErrorView()
      !state.loadingAccessManagementList -> genericErrorView {
        title = Strings.access_control_not_configured_yet.get()
        subtitle = Strings.access_control_not_configured_yet_subtitle.get()
      }
    }
  }
}

fun ChildrenBuilder.renderAccessManagementList(handler: ListAccessManagementProps.() -> Unit) {
  ListAccessManagement::class.react {
    +jso(handler)
  }
}
