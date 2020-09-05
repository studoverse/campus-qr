package views.accessManagement.accessManagementOverview

import MenuItem
import apiBase
import com.studo.campusqr.common.ClientAccessManagement
import materialMenu
import react.*
import util.Strings
import util.get
import views.accessManagement.AccessManagementDetailsProps
import views.accessManagement.renderAccessManagementDetails
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbMaterialDialog

interface AccessManagementTableRowProps : RProps {
  class Config(
      val accessManagement: ClientAccessManagement,
      val onEditFinished: (response: String?) -> Unit,
      val onDeleteFinsihed: (success: Boolean) -> Unit,
      val onCopyFinished: (success: Boolean) -> Unit
  )

  var config: Config
  var classes: AccessManagementTableRowClasses
}

interface AccessManagementTableRowState : RState {
  var showEditAccessManagementDialog: Boolean
  var showDetailsAccessManagementDialog: Boolean
  var working: Boolean
}

class AccessManagementTableRow : RComponent<AccessManagementTableRowProps, AccessManagementTableRowState>() {

  override fun AccessManagementTableRowState.init() {
    showEditAccessManagementDialog = false
    showDetailsAccessManagementDialog = false
    working = false
  }

  private fun RBuilder.renderEditAccessManagementDialog() = mbMaterialDialog(
      show = true,
      title = Strings.location_edit.get(),
      customContent = {
        renderAccessManagementDetails(AccessManagementDetailsProps.Config.Edit(
            accessManagement = props.config.accessManagement,
            onEdited = { success ->
              // TODO: Prooagate back to AccessOverview
              props.config.onEditFinished("ok")
              setState {
                showEditAccessManagementDialog = false
              }
            }))
      },
      buttons = null,
      onClose = {
        setState {
          showEditAccessManagementDialog = false
        }
      }
  )

  private fun RBuilder.renderDetailsAccessManagementDialog() = mbMaterialDialog(
      show = true,
      title = "Access control details",
      customContent = {
        renderAccessManagementDetails(AccessManagementDetailsProps.Config.Details(
            accessManagement = props.config.accessManagement,
        ))
      },
      buttons = null,
      onClose = {
        setState {
          showDetailsAccessManagementDialog = false
        }
      }
  )

  override fun RBuilder.render() {
    if (state.showEditAccessManagementDialog) {
      renderEditAccessManagementDialog()
    }
    if (state.showDetailsAccessManagementDialog) {
      renderDetailsAccessManagementDialog()
    }
    mTableRow {
      val tableRowClick = {
        setState {
          showDetailsAccessManagementDialog = true
        }
      }
      attrs.hover = true
      // TODO: Click to see the details/edit

      mTableCell {
        attrs.onClick = tableRowClick
        +props.config.accessManagement.locationName
      }
      mTableCell {
        attrs.onClick = tableRowClick
        +props.config.accessManagement.dateRanges.toString() // TODO better formatting
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
        if (state.working) {
          circularProgress {}
        } else {
          materialMenu(
            menuItems = listOf(
              MenuItem(text = Strings.edit.get(), icon = editIcon, onClick = {
                setState {
                  showEditAccessManagementDialog = true
                }
              }),
              MenuItem(text = Strings.delete.get(), icon = deleteIcon, onClick = {
                launch {
                  val response =
                    NetworkManager.get<String>("$apiBase/access/${props.config.accessManagement.id}/delete")
                  props.config.onDeleteFinsihed(response == "ok")
                }
              }),
              MenuItem(text = Strings.copy.get(), icon = fileCopyOutlinedIcon, onClick = {
                launch {
                  val response =
                    NetworkManager.get<String>("$apiBase/access/${props.config.accessManagement.id}/duplicate")
                  props.config.onDeleteFinsihed(response == "ok")
                }
              }),
            )
          )
        }
      }
    }
  }
}

interface AccessManagementTableRowClasses {
  // Keep in sync with LocationTableRowStyle!
}

private val LocationTableRowStyle = { theme: dynamic ->
  // Keep in sync with LocationTableRowClasses!
}

private val styled = withStyles<AccessManagementTableRowProps, AccessManagementTableRow>(LocationTableRowStyle)

fun RBuilder.renderAccessManagementRow(config: AccessManagementTableRowProps.Config) = styled {
  attrs.config = config
}
