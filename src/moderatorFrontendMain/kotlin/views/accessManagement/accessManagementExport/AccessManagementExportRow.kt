package views.accessManagement.accessManagementExport

import com.studo.campusqr.common.AccessManagementExportData
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.strong
import views.accessManagement.accessManagementOverview.format
import webcore.materialUI.mTableCell
import webcore.materialUI.mTableRow
import webcore.materialUI.withStyles
import kotlin.js.Date

interface AccessManagementExportTableRowProps : RProps {
  class Config(
    val permit: AccessManagementExportData.Permit,
  )

  var config: Config
  var classes: AccessManagementExportTableRowClasses
}

interface AccessManagementExportTableRowState : RState {
  var showEditAccessManagementDialog: Boolean
  var showDetailsAccessManagementDialog: Boolean
  var working: Boolean
}

class AccessManagementExportTableRow :
  RComponent<AccessManagementExportTableRowProps, AccessManagementExportTableRowState>() {

  override fun AccessManagementExportTableRowState.init() {
    showEditAccessManagementDialog = false
    showDetailsAccessManagementDialog = false
    working = false
  }

  override fun RBuilder.render() {
    mTableRow {
      attrs.hover = true

      mTableCell {
        val dateRange = props.config.permit.dateRange
        val now = Date().getTime()
        if (dateRange.from < now && dateRange.to > now) {
          // Current date range
          strong {
            +dateRange.format()
          }
        } else {
          // Date range in past
          +dateRange.format()
        }
      }
      mTableCell {
        +props.config.permit.email
      }
    }
  }
}

interface AccessManagementExportTableRowClasses {
  // Keep in sync with LocationTableRowStyle!
}

private val LocationTableRowStyle = { theme: dynamic ->
  // Keep in sync with LocationTableRowClasses!
}

private val styled =
  withStyles<AccessManagementExportTableRowProps, AccessManagementExportTableRow>(LocationTableRowStyle)

fun RBuilder.renderAccessManagementExportRow(config: AccessManagementExportTableRowProps.Config) = styled {
  attrs.config = config
}