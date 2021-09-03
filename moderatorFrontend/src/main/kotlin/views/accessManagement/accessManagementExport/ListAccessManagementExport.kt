package views.accessManagement.accessManagementExport

import com.studo.campusqr.common.payloads.AccessManagementExportData
import com.studo.campusqr.common.payloads.ClientLocation
import kotlinext.js.js
import react.*
import util.Strings
import util.Url
import util.apiBase
import util.get
import views.common.ToolbarViewProps
import views.common.networkErrorView
import views.common.renderLinearProgress
import views.common.renderToolbarView
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*

interface ListAccessManagementExportProps : RProps {
  var classes: ListAccessManagementExportClasses
  var locationId: String?
}

interface ListAccessManagementExportState : RState {
  var permitList: List<AccessManagementExportData.Permit>?
  var clientLocation: ClientLocation?
  var loadingPermitList: Boolean
}

class ListAccessManagementExport : RComponent<ListAccessManagementExportProps, ListAccessManagementExportState>() {

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

  override fun RBuilder.render() {
    renderToolbarView(
      ToolbarViewProps.Config(
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

    renderLinearProgress(state.loadingPermitList)

    if (state.permitList != null) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.access_control_time_slot.get() }
            mTableCell { +Strings.access_control_permitted_person.get() }
          }
        }
        mTableBody {
          state.permitList!!.forEach { accessManagement ->
            renderAccessManagementExportRow(AccessManagementExportTableRowProps.Config(accessManagement))
          }
        }
      }
    } else if (!state.loadingPermitList) {
      networkErrorView()
    }
  }
}

interface ListAccessManagementExportClasses {
  var header: String
}

private val style = { _: dynamic ->
  js {
    header = js {
      margin = 16
    }
  }
}

private val styled = withStyles<ListAccessManagementExportProps, ListAccessManagementExport>(style)

fun RBuilder.renderAccessManagementExportList(locationId: String?) = styled {
  // Set component attrs here
  attrs.locationId = locationId
}
