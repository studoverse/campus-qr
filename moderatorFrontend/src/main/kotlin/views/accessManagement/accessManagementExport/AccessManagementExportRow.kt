package views.accessManagement.accessManagementExport

import com.studo.campusqr.common.payloads.AccessManagementExportData
import kotlinx.js.jso
import mui.material.TableCell
import mui.material.TableRow
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.html.ReactHTML.strong
import react.react
import views.accessManagement.accessManagementOverview.format
import webcore.RComponent
import kotlin.js.Date

class AccessManagementExportTableRowConfig(
  val permit: AccessManagementExportData.Permit,
)

external interface AccessManagementExportTableRowProps : Props {
  var config: AccessManagementExportTableRowConfig
}

external interface AccessManagementExportTableRowState : State

class AccessManagementExportTableRow :
  RComponent<AccessManagementExportTableRowProps, AccessManagementExportTableRowState>() {

  override fun ChildrenBuilder.render() {
    TableRow {
      hover = true

      TableCell {
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
      TableCell {
        +props.config.permit.email
      }
    }
  }
}

fun ChildrenBuilder.renderAccessManagementExportRow(handler: AccessManagementExportTableRowProps.() -> Unit) {
  AccessManagementExportTableRow::class.react {
    +jso(handler)
  }
}