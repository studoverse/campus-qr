package views.accessManagement.accessManagementExport

import com.studo.campusqr.common.payloads.AccessManagementExportData
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.strong
import views.accessManagement.accessManagementOverview.format
import webcore.materialUI.mTableCell
import webcore.materialUI.mTableRow
import webcore.materialUI.withStyles
import kotlin.js.Date

class AccessManagementExportTableRowConfig(
  val permit: AccessManagementExportData.Permit,
)

external interface AccessManagementExportTableRowProps : Props {
  var config: AccessManagementExportTableRowConfig
  var classes: AccessManagementExportTableRowClasses
}

external interface AccessManagementExportTableRowState : State

class AccessManagementExportTableRow :
  RComponent<AccessManagementExportTableRowProps, AccessManagementExportTableRowState>() {

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

external interface AccessManagementExportTableRowClasses

private val style = { _: dynamic ->
}

private val styled = withStyles<AccessManagementExportTableRowProps, AccessManagementExportTableRow>(style)

fun RBuilder.renderAccessManagementExportRow(config: AccessManagementExportTableRowConfig) = styled {
  attrs.config = config
}