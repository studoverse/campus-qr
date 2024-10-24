package views.accessManagement.accessManagementExport

import mui.material.*
import react.Props
import util.Strings
import util.Url
import util.get
import views.common.MbLinearProgressFc
import views.common.ToolbarViewConfig
import views.common.ToolbarViewFc
import views.common.networkErrorView
import webcore.FcWithCoroutineScope

external interface ListAccessManagementExportProps : Props {
  var locationId: String?
}

val ListAccessManagementExport = FcWithCoroutineScope<ListAccessManagementExportProps> { props, launch ->
  val controller = ListAccessManagementExportController.useListAccessManagementExportController(props = props, launch = launch)

  ToolbarViewFc {
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

  MbLinearProgressFc { show = controller.loadingPermitList }

  if (controller.permitList != null) {
    Table {
      TableHead {
        TableRow {
          TableCell { +Strings.access_control_time_slot.get() }
          TableCell { +Strings.access_control_permitted_person.get() }
        }
      }
      TableBody {
        controller.permitList!!.forEach { accessManagement ->
          AccessManagementExportTableRow { config = AccessManagementExportTableRowConfig(accessManagement) }
        }
      }
    }
  } else if (!controller.loadingPermitList) {
    networkErrorView()
  }
}
