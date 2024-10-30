package views.accessManagement.accessManagementOverview.accessManagementRow

import js.lazy.Lazy
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
import views.accessManagement.accessManagementOverview.accessManagementRow.AccessManagementTableRowController.Companion.format
import web.html.HTMLTableCellElement
import webcore.*
import webcore.materialMenu.MaterialMenu
import webcore.materialMenu.MaterialMenuConfig
import webcore.materialMenu.MaterialMenuConfig.Companion.MenuItem
import kotlin.js.Date

external interface AccessManagementTableRowProps : Props {
  var config: AccessManagementTableRowConfig
}

@Lazy
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
          Suspense {
            AccessManagementDetailsFc {
              config = AccessManagementDetailsConfig.Details(
                accessManagement = props.config.accessManagement,
                dialogRef = props.config.dialogRef,
              )
            }
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
          Suspense {
            AccessManagementDetailsFc {
              config = AccessManagementDetailsConfig.Edit(
                accessManagement = props.config.accessManagement,
                dialogRef = props.config.dialogRef,
                onEdited = { success ->
                  props.config.onOperationFinished(AccessManagementTableRowOperation.Edit, success)
                }
              )
            }
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
      Suspense {
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
}
