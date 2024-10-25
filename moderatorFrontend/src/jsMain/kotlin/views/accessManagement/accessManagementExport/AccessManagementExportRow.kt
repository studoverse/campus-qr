package views.accessManagement.accessManagementExport

import com.studo.campusqr.common.payloads.AccessManagementExportData
import mui.material.TableCell
import mui.material.TableRow
import react.Props
import react.dom.html.ReactHTML.strong
import views.accessManagement.accessManagementOverview.accessManagementRow.format
import webcore.FcWithCoroutineScope
import kotlin.js.Date

class AccessManagementExportTableRowConfig(
  val permit: AccessManagementExportData.Permit,
)

external interface AccessManagementExportTableRowProps : Props {
  var config: AccessManagementExportTableRowConfig
}

val AccessManagementExportTableRow = FcWithCoroutineScope<AccessManagementExportTableRowProps> { props, launch ->
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
