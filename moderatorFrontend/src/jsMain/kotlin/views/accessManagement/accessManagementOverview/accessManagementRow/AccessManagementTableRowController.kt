package views.accessManagement.accessManagementOverview.accessManagementRow

import util.Strings
import util.apiBase
import util.get
import web.prompts.confirm
import webcore.Launch
import webcore.MenuItemOnClick
import webcore.NetworkManager

data class AccessManagementTableRowController(
  val duplicateMenuItemOnClick: MenuItemOnClick,
  val deleteMenuItemOnClick: MenuItemOnClick,
) {
  companion object {
    fun useAccessManagementRowController(launch: Launch, config: AccessManagementTableRowConfig): AccessManagementTableRowController {

      val duplicateMenuItemOnClick: MenuItemOnClick = {
        launch {
          val response = NetworkManager.post<String>("$apiBase/access/${config.accessManagement.id}/duplicate")
          config.onOperationFinished(AccessManagementTableRowOperation.Duplicate, response == "ok")
        }
      }

      val deleteMenuItemOnClick: MenuItemOnClick = {
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
  }
}