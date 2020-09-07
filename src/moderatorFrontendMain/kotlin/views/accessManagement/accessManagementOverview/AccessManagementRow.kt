package views.accessManagement.accessManagementOverview

import MenuItem
import apiBase
import com.studo.campusqr.common.ClientAccessManagement
import com.studo.campusqr.common.ClientDateRange
import kotlinx.browser.window
import materialMenu
import react.*
import react.dom.br
import react.dom.strong
import util.Strings
import util.get
import views.accessManagement.AccessManagementDetailsProps
import views.accessManagement.accessManagementOverview.AccessManagementTableRowProps.Config
import views.accessManagement.accessManagementOverview.AccessManagementTableRowProps.Operation
import views.accessManagement.renderAccessManagementDetails
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.extensions.twoDigitString
import webcore.materialUI.*
import webcore.mbMaterialDialog
import kotlin.js.Date

interface AccessManagementTableRowProps : RProps {
  enum class Operation {
    Edit, Delete, Duplicate
  }

  class Config(
      val accessManagement: ClientAccessManagement,
      val onOperationFinished: (operation: Operation, success: Boolean) -> Unit
  )

  var config: Config
  var classes: AccessManagementTableRowClasses
}

interface AccessManagementTableRowState : RState {
  var showAccessManagementEditDialog: Boolean
  var showAccessManagementDetailsDialog: Boolean
  var showProgress: Boolean
}

class AccessManagementTableRow : RComponent<AccessManagementTableRowProps, AccessManagementTableRowState>() {

  override fun AccessManagementTableRowState.init() {
    showAccessManagementEditDialog = false
    showAccessManagementDetailsDialog = false
    showProgress = false
  }

  private fun RBuilder.renderEditAccessManagementDialog() = mbMaterialDialog(
      show = true,
      title = Strings.location_edit.get(),
      customContent = {
        renderAccessManagementDetails(AccessManagementDetailsProps.Config.Edit(
            accessManagement = props.config.accessManagement,
            onEdited = { success ->
              props.config.onOperationFinished(Operation.Edit, success)
              setState {
                showAccessManagementEditDialog = false
              }
            }))
      },
      buttons = null,
      onClose = {
        setState {
          showAccessManagementEditDialog = false
        }
      }
  )

  private fun RBuilder.renderDetailsAccessManagementDialog() = mbMaterialDialog(
    show = true,
    title = Strings.access_control.get(),
    customContent = {
      renderAccessManagementDetails(
        AccessManagementDetailsProps.Config.Details(
          accessManagement = props.config.accessManagement,
        )
      )
    },
    buttons = null,
    onClose = {
      setState {
        showAccessManagementDetailsDialog = false
      }
    }
  )

  override fun RBuilder.render() {
    if (state.showAccessManagementEditDialog) {
      renderEditAccessManagementDialog()
    }
    if (state.showAccessManagementDetailsDialog) {
      renderDetailsAccessManagementDialog()
    }
    mTableRow {
      val tableRowClick = {
        setState {
          showAccessManagementDetailsDialog = true
        }
      }
      attrs.hover = true

      mTableCell {
        attrs.onClick = tableRowClick
        +props.config.accessManagement.locationName
      }
      mTableCell {
        attrs.onClick = tableRowClick
        val dateRanges = props.config.accessManagement.dateRanges
        val now = Date().getTime()
        dateRanges.forEachIndexed { index, dateRange ->
          if (index != 0) {
            br { }
          }
          if (dateRange.from < now && dateRange.to > now) {
            // Current date range
            strong {
              +dateRange.format()
            }
          } else {
            // Date range in past or future
            +dateRange.format()
          }
        }
      }
      mTableCell {
        attrs.onClick = tableRowClick
        +props.config.accessManagement.allowedEmails.count().toString()
      }
      mTableCell {
        attrs.onClick = tableRowClick
        +props.config.accessManagement.note
      }
      mTableCell {
        if (state.showProgress) {
          circularProgress {}
        } else {
          materialMenu(
            menuItems = listOf(
              MenuItem(text = Strings.edit.get(), icon = editIcon, onClick = {
                setState {
                  showAccessManagementEditDialog = true
                }
              }),
              MenuItem(text = Strings.duplicate.get(), icon = fileCopyOutlinedIcon, onClick = {
                launch {
                  val response =
                    NetworkManager.get<String>("$apiBase/access/${props.config.accessManagement.id}/duplicate")
                  props.config.onOperationFinished(Operation.Duplicate, response == "ok")
                }
              }),
              MenuItem(text = Strings.delete.get(), icon = deleteIcon, onClick = {
                if (window.confirm(Strings.access_control_delete_are_your_sure.get())) {
                  launch {
                    val response =
                      NetworkManager.get<String>("$apiBase/access/${props.config.accessManagement.id}/delete")
                    props.config.onOperationFinished(Operation.Delete, response == "ok")
                  }
                }
              }),
            )
          )
        }
      }
    }
  }
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


interface AccessManagementTableRowClasses {
  // Keep in sync with LocationTableRowStyle!
}

private val LocationTableRowStyle = { theme: dynamic ->
  // Keep in sync with LocationTableRowClasses!
}

private val styled = withStyles<AccessManagementTableRowProps, AccessManagementTableRow>(LocationTableRowStyle)

fun RBuilder.renderAccessManagementRow(config: Config) = styled {
  attrs.config = config
}