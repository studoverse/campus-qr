package views.accessManagement.accessManagementOverview

import com.studo.campusqr.common.payloads.AccessManagementData
import com.studo.campusqr.common.payloads.ClientAccessManagement
import com.studo.campusqr.common.payloads.ClientLocation
import react.*
import util.*
import views.accessManagement.AccessManagementDetailsConfig
import views.accessManagement.renderAccessManagementDetails
import views.common.*
import webcore.MbSnackbarConfig
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbMaterialDialog
import webcore.mbSnackbar

external interface ListAccessManagementProps : Props {
  var classes: ListAccessManagementClasses
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

  private fun RBuilder.renderAddAccessManagementDialog() = mbMaterialDialog(
    show = state.showAddAccessManagementDialog,
    title = Strings.access_control_create.get(),
    customContent = {
      renderAccessManagementDetails(
        AccessManagementDetailsConfig.Create(
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
      )
    },
    buttons = null,
    onClose = {
      setState {
        showAddAccessManagementDialog = false
      }
    }
  )

  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarConfig(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      })
  )

  override fun RBuilder.render() {
    renderAddAccessManagementDialog()
    renderSnackbar()
    renderToolbarView(
      ToolbarViewConfig(
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
          ToolbarViewButton(
            text = Strings.access_control_export.get(),
            variant = "outlined",
            onClick = { routeContext ->
              if (props.locationId == null) {
                routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LIST_EXPORT.toRoute()!!)
              } else {
                routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT.toRoute(pathParams = mapOf("id" to props.locationId!!))!!)
              }
            }
          ),
          ToolbarViewButton(
            text = Strings.access_control_create.get(),
            variant = "contained",
            onClick = { _ ->
              setState {
                showAddAccessManagementDialog = true
              }
            }
          )
        )
      )
    )

    renderLinearProgress(state.loadingAccessManagementList)

    when {
      state.accessManagementList?.isNotEmpty() == true -> mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.location_name.get() }
            mTableCell { +Strings.access_control_time_slots.get() }
            mTableCell { +Strings.access_control_permitted_people.get() }
            mTableCell { +Strings.access_control_note.get() }
            mTableCell { +Strings.actions.get() }
          }
        }
        mTableBody {
          state.accessManagementList!!.forEach { accessManagement ->
            renderAccessManagementRow(
              AccessManagementTableRowConfig(
                accessManagement,
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
            )
          }
        }
      }
      state.accessManagementList == null && !state.loadingAccessManagementList -> networkErrorView()
      !state.loadingAccessManagementList -> genericErrorView(
        Strings.access_control_not_configured_yet.get(),
        Strings.access_control_not_configured_yet_subtitle.get()
      )
    }
  }
}

external interface ListAccessManagementClasses

private val style = { _: dynamic ->
}

private val styled = withStyles<ListAccessManagementProps, ListAccessManagement>(style)

fun RBuilder.renderAccessManagementList(locationId: String?) = styled {
  // Set component attrs here
  attrs.locationId = locationId
}
