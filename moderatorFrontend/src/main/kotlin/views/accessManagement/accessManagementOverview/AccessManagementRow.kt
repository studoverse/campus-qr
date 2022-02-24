package views.accessManagement.accessManagementOverview

import com.studo.campusqr.common.payloads.ClientAccessManagement
import com.studo.campusqr.common.payloads.ClientDateRange
import kotlinx.browser.window
import react.*
import react.dom.br
import react.dom.strong
import util.Strings
import util.apiBase
import util.get
import views.accessManagement.AccessManagementDetailsConfig
import views.accessManagement.renderAccessManagementDetails
import webcore.MenuItem
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.extensions.twoDigitString
import webcore.materialMenu
import webcore.materialUI.*
import webcore.mbMaterialDialog
import kotlin.js.Date

enum class AccessManagementTableRowOperation {
  Edit, Delete, Duplicate
}

class AccessManagementTableRowConfig(
  val accessManagement: ClientAccessManagement,
  val onOperationFinished: (operation: AccessManagementTableRowOperation, success: Boolean) -> Unit
)

external interface AccessManagementTableRowProps : RProps {
  var config: AccessManagementTableRowConfig
  var classes: AccessManagementTableRowClasses
}

external interface AccessManagementTableRowState : RState {
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
      renderAccessManagementDetails(
        AccessManagementDetailsConfig.Edit(
          accessManagement = props.config.accessManagement,
          onEdited = { success ->
            props.config.onOperationFinished(AccessManagementTableRowOperation.Edit, success)
            setState {
              showAccessManagementEditDialog = false
            }
          })
      )
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
        AccessManagementDetailsConfig.Details(
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
                  val response = NetworkManager.post<String>("$apiBase/access/${props.config.accessManagement.id}/duplicate")
                  props.config.onOperationFinished(AccessManagementTableRowOperation.Duplicate, response == "ok")
                }
              }),
              MenuItem(text = Strings.delete.get(), icon = deleteIcon, onClick = {
                if (window.confirm(Strings.access_control_delete_are_your_sure.get())) {
                  launch {
                    val response = NetworkManager.post<String>("$apiBase/access/${props.config.accessManagement.id}/delete")
                    props.config.onOperationFinished(AccessManagementTableRowOperation.Delete, response == "ok")
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


external interface AccessManagementTableRowClasses

private val style = { _: dynamic ->
}

private val styled = withStyles<AccessManagementTableRowProps, AccessManagementTableRow>(style)

fun RBuilder.renderAccessManagementRow(config: AccessManagementTableRowConfig) = styled {
  attrs.config = config
}