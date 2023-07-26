package views.accessManagement.accessManagementExport

import com.studo.campusqr.common.payloads.AccessManagementExportData
import com.studo.campusqr.common.payloads.ClientLocation
import mui.material.*
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.Url
import util.apiBase
import util.get
import views.common.ToolbarViewConfig
import views.common.networkErrorView
import views.common.renderMbLinearProgress
import views.common.renderToolbarView
import webcore.NetworkManager
import webcore.RComponent
import webcore.extensions.launch
import webcore.setState

external interface ListAccessManagementExportProps : Props {
  var locationId: String?
}

external interface ListAccessManagementExportState : State {
  var permitList: List<AccessManagementExportData.Permit>?
  var clientLocation: ClientLocation?
  var loadingPermitList: Boolean
}

private class ListAccessManagementExport : RComponent<ListAccessManagementExportProps, ListAccessManagementExportState>() {

  override fun ListAccessManagementExportState.init() {
    permitList = null
    clientLocation = null
    loadingPermitList = false
  }

  private fun fetchAccessManagementList() = launch {
    setState { loadingPermitList = true }
    val params = props.locationId?.let { "?locationId=$it" } ?: ""
    val response = NetworkManager.get<AccessManagementExportData>("$apiBase/access/export$params")
    setState {
      permitList = response?.permits?.toList()
      clientLocation = response?.clientLocation
      loadingPermitList = false
    }
  }

  override fun componentDidUpdate(
    prevProps: ListAccessManagementExportProps, prevState: ListAccessManagementExportState,
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

  override fun ChildrenBuilder.render() {
    renderToolbarView(
      config = ToolbarViewConfig(
        title = StringBuilder().apply {
          append(Strings.access_control_export.get())
          append(" - ")
          if (state.clientLocation == null) {
            append(Strings.access_control_my.get())
          } else {
            append(state.clientLocation!!.name)
          }
        }.toString(),
        backButtonUrl = Url.ACCESS_MANAGEMENT_LIST,
        buttons = emptyList()
      )
    )

    renderMbLinearProgress(show = state.loadingPermitList)

    if (state.permitList != null) {
      Table {
        TableHead {
          TableRow {
            TableCell { +Strings.access_control_time_slot.get() }
            TableCell { +Strings.access_control_permitted_person.get() }
          }
        }
        TableBody {
          state.permitList!!.forEach { accessManagement ->
            renderAccessManagementExportRow(config = AccessManagementExportTableRowConfig(accessManagement))
          }
        }
      }
    } else if (!state.loadingPermitList) {
      networkErrorView()
    }
  }
}

fun ChildrenBuilder.renderAccessManagementExportList(locationId: String?) {
  ListAccessManagementExport::class.react {
    this.locationId = locationId
  }
}
