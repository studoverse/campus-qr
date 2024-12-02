package views.accessManagement.accessManagementExport.listAccessManagementExport

import js.lazy.Lazy
import mui.material.*
import react.Props
import react.Suspense
import util.Strings
import util.Url
import util.get
import views.accessManagement.accessManagementExport.accessManagementExportTableRow.AccessManagementExportTableRow
import views.accessManagement.accessManagementExport.accessManagementExportTableRow.AccessManagementExportTableRowConfig
import views.common.MbLinearProgress
import views.common.ToolbarView.ToolbarViewConfig
import views.common.ToolbarView.ToolbarView
import views.common.networkErrorView
import webcore.FcWithCoroutineScope

external interface ListAccessManagementExportProps : Props {
  var config: ListAccessManagementExportConfig
}

@Lazy
val ListAccessManagementExport = FcWithCoroutineScope<ListAccessManagementExportProps> { props, launch ->
  val controller = ListAccessManagementExportController.use(props = props, launch = launch)

  Suspense {
    ToolbarView {
      config = ToolbarViewConfig(
        title = StringBuilder().apply {
          append(Strings.access_control_export.get())
          append(" - ")
          if (controller.clientLocation == null) {
            append(Strings.access_control_my.get())
          } else {
            append(controller.clientLocation!!.name)
          }
        }.toString(),
        backButtonUrl = Url.ACCESS_MANAGEMENT_LIST,
        buttons = emptyList()
      )
    }
  }

  MbLinearProgress { show = controller.loadingPermitList }

  if (controller.permitList != null) {
    Table {
      TableHead {
        TableRow {
          TableCell { +Strings.access_control_time_slot.get() }
          TableCell { +Strings.access_control_permitted_person.get() }
        }
      }
      TableBody {
        Suspense {
          controller.permitList!!.forEach { accessManagement ->
            AccessManagementExportTableRow { config = AccessManagementExportTableRowConfig(accessManagement) }
          }
        }
      }
    }
  } else if (!controller.loadingPermitList) {
    networkErrorView()
  }
}
