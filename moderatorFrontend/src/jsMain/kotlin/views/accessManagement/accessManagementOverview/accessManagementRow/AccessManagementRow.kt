package views.accessManagement.accessManagementOverview.accessManagementRow

import com.studo.campusqr.common.payloads.ClientAccessManagement
import com.studo.campusqr.common.payloads.ClientDateRange
import mui.icons.material.Delete
import mui.icons.material.Edit
import mui.icons.material.FileCopyOutlined
import mui.material.TableCell
import mui.material.TableRow
import react.*
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.strong
import util.Strings
import util.get
import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import views.accessManagement.accessManagementDetails.AccessManagementDetailsFc
import web.html.HTMLTableCellElement
import webcore.*
import webcore.MaterialMenu
import webcore.extensions.twoDigitString
import kotlin.js.Date

class AccessManagementTableRowConfig(
  val accessManagement: ClientAccessManagement,
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onOperationFinished: (operation: AccessManagementTableRowOperation, success: Boolean) -> Unit
)

enum class AccessManagementTableRowOperation {
  Edit, Delete, Duplicate
}

external interface AccessManagementTableRowProps : Props {
  var config: AccessManagementTableRowConfig
}

val AccessManagementTableRowFc = FcWithCoroutineScope<AccessManagementTableRowProps> { props, launch ->
  val controller = AccessManagementTableRowController.useAccessManagementRowController(
    launch = launch,
    config = props.config
  )

  fun renderDetailsAccessManagementDialog() {
    props.config.dialogRef.current!!.showDialog(
      DialogConfig(
        title = DialogConfig.Title(text = Strings.access_control.get()),
        customContent = {
          AccessManagementDetailsFc {
            config = AccessManagementDetailsConfig.Details(
              accessManagement = props.config.accessManagement,
              dialogRef = props.config.dialogRef,
            )
          }
        },
      )
    )
  }

  fun renderEditAccessManagementDialog() {
    props.config.dialogRef.current!!.showDialog(
      DialogConfig(
        title = DialogConfig.Title(text = Strings.location_edit.get()),
        customContent = {
          AccessManagementDetailsFc {
            config = AccessManagementDetailsConfig.Edit(
              accessManagement = props.config.accessManagement,
              dialogRef = props.config.dialogRef,
              onEdited = { success ->
                props.config.onOperationFinished(AccessManagementTableRowOperation.Edit, success)
              }
            )
          }
        },
      )
    )
  }

  TableRow {
    val tableRowClick = { _: MouseEvent<HTMLTableCellElement, *> ->
      renderDetailsAccessManagementDialog()
    }

    hover = true

    TableCell {
      onClick = tableRowClick
      +props.config.accessManagement.locationName
    }
    TableCell {
      onClick = tableRowClick
      val dateRanges = props.config.accessManagement.dateRanges
      val now = Date().getTime()
      dateRanges.forEachIndexed { index, dateRange ->
        if (index != 0) {
          verticalMargin(16)
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
    TableCell {
      onClick = tableRowClick
      +props.config.accessManagement.allowedEmails.count().toString()
    }
    TableCell {
      onClick = tableRowClick
      +props.config.accessManagement.note
    }
    TableCell {
      MaterialMenu {
        config = MaterialMenuConfig(
          menuItems = listOf(
            MenuItem(text = Strings.edit.get(), icon = Edit, onClick = { renderEditAccessManagementDialog() }),
            MenuItem(text = Strings.duplicate.get(), icon = FileCopyOutlined, onClick = controller.duplicateMenuItemOnClick),
            MenuItem(text = Strings.delete.get(), icon = Delete, onClick = controller.deleteMenuItemOnClick),
          )
        )
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