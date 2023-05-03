package views.accessManagement.accessManagementOverview

import app.AppContext
import app.appContextToInject
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
import util.apiBase
import util.get
import views.accessManagement.AccessManagementDetailsConfig
import views.accessManagement.AddLocation
import web.html.HTMLTableCellElement
import web.prompts.confirm
import webcore.*
import webcore.extensions.launch
import webcore.extensions.twoDigitString
import kotlin.js.Date

class AccessManagementTableRowConfig(
  val accessManagement: ClientAccessManagement,
  val onOperationFinished: (operation: AccessManagementTableRowOperation, success: Boolean) -> Unit
)

enum class AccessManagementTableRowOperation {
  Edit, Delete, Duplicate
}

external interface AccessManagementTableRowProps : Props {
  var config: AccessManagementTableRowConfig
}

external interface AccessManagementTableRowState : State

private class AccessManagementTableRow : RComponent<AccessManagementTableRowProps, AccessManagementTableRowState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AccessManagementTableRow::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  private fun renderEditAccessManagementDialog() {
    appContext.showDialog(
      DialogConfig(
        title = Strings.location_edit.get(),
        customContent = DialogConfig.CustomContent(AddLocation::class) {
          config = AccessManagementDetailsConfig.Edit(
            accessManagement = props.config.accessManagement,
            onEdited = { success ->
              props.config.onOperationFinished(AccessManagementTableRowOperation.Edit, success)
            }
          )
        },
      )
    )
  }

  private fun renderDetailsAccessManagementDialog() = appContext.showDialog(
    DialogConfig(
      title = Strings.access_control.get(),
      customContent = DialogConfig.CustomContent(AddLocation::class) {
        config = AccessManagementDetailsConfig.Details(
          accessManagement = props.config.accessManagement,
        )
      },
    )
  )

  override fun ChildrenBuilder.render() {
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
        materialMenu(
          config = MaterialMenuConfig(
            menuItems = listOf(
              MenuItem(text = Strings.edit.get(), icon = Edit, onClick = {
                renderEditAccessManagementDialog()
              }),
              MenuItem(text = Strings.duplicate.get(), icon = FileCopyOutlined, onClick = {
                launch {
                  val response = NetworkManager.post<String>("$apiBase/access/${props.config.accessManagement.id}/duplicate")
                  props.config.onOperationFinished(AccessManagementTableRowOperation.Duplicate, response == "ok")
                }
              }),
              MenuItem(text = Strings.delete.get(), icon = Delete, onClick = {
                if (confirm(Strings.access_control_delete_are_your_sure.get())) {
                  launch {
                    val response = NetworkManager.post<String>("$apiBase/access/${props.config.accessManagement.id}/delete")
                    props.config.onOperationFinished(AccessManagementTableRowOperation.Delete, response == "ok")
                  }
                }
              }),
            )
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

fun ChildrenBuilder.renderAccessManagementRow(config: AccessManagementTableRowConfig) {
  AccessManagementTableRow::class.react {
    this.config = config
  }
}
