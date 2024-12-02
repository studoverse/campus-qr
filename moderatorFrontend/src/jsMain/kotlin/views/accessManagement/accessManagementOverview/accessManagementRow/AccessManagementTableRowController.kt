package views.accessManagement.accessManagementOverview.accessManagementRow

import com.studo.campusqr.common.payloads.ClientDateRange
import util.Strings
import util.apiBase
import util.get
import web.prompts.confirm
import webcore.Launch
import webcore.NetworkManager
import webcore.extensions.twoDigitString
import kotlin.js.Date

data class AccessManagementTableRowController(
  val duplicateMenuItemOnClick: () -> Unit,
  val deleteMenuItemOnClick: () -> Unit,
) {
  companion object {
    fun use(launch: Launch, config: AccessManagementTableRowConfig): AccessManagementTableRowController {
      val duplicateMenuItemOnClick: () -> Unit = {
        launch {
          val response = NetworkManager.post<String>("$apiBase/access/${config.accessManagement.id}/duplicate")
          config.onOperationFinished(AccessManagementTableRowOperation.Duplicate, response == "ok")
        }
      }

      val deleteMenuItemOnClick = {
        if (confirm(Strings.access_control_delete_are_your_sure.get())) {
          launch {
            val response = NetworkManager.post<String>("$apiBase/access/${config.accessManagement.id}/delete")
            config.onOperationFinished(AccessManagementTableRowOperation.Delete, response == "ok")
          }
        }
      }

      return AccessManagementTableRowController(
        duplicateMenuItemOnClick = duplicateMenuItemOnClick,
        deleteMenuItemOnClick = deleteMenuItemOnClick,
      )
    }

    private fun Date.format(showDate: Boolean = true): String {
      val day = this.getDate().twoDigitString()
      val month = (this.getMonth() + 1).twoDigitString()
      val year = this.getFullYear()

      val date = "$day.$month.${if (year != Date().getFullYear()) year else ""}"

      val hour = this.getHours().twoDigitString()
      val minutes = this.getMinutes().twoDigitString()
      val time = "$hour:$minutes"

      return if (showDate) "$date $time" else time
    }

    fun ClientDateRange.format(): String {
      val fromDate = Date(from)
      val toDate = Date(to)

      return if (fromDate.toDateString() == toDate.toDateString()) {
        // 10.11. 13:00 - 14:00
        fromDate.format() + " - " + toDate.format(showDate = false)
      } else {
        // 10.11 13:00 - 11.11. 13:00
        Date(from).format() + " - " + Date(to).format()
      }
    }
  }
}