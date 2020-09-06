package views.accessManagement.accessManagementExport

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.AccessManagementExportData
import com.studo.campusqr.common.ClientLocation
import kotlinext.js.js
import react.*
import react.dom.div
import util.Strings
import util.get
import views.common.networkErrorView
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
    div(GlobalCss.flex) {
      typography {
        attrs.className = props.classes.header
        attrs.variant = "h5"
        +Strings.access_control_export.get()
        +" - "
        if (state.clientLocation == null) {
          +Strings.access_control_my.get()
        } else {
          +state.clientLocation!!.name
        }
      }
    }

    div(props.classes.progressHolder) {
      if (state.loadingPermitList) {
        linearProgress {}
      }
    }

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
  var progressHolder: String
}

private val ListAccessManagementExportStyle = { theme: dynamic ->
  js {
    header = js {
      margin = 16
    }
    progressHolder = js {
      height = 8
    }
  }
}

private val styled =
  withStyles<ListAccessManagementExportProps, ListAccessManagementExport>(ListAccessManagementExportStyle)

fun RBuilder.renderAccessManagementExportList(locationId: String?) = styled {
  // Set component attrs here
  attrs.locationId = locationId
}
