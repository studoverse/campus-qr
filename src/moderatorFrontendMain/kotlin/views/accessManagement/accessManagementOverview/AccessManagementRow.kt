package views.accessManagement.accessManagementOverview

import MenuItem
import com.studo.campusqr.common.ClientAccessManagement
import materialMenu
import react.*
import util.Strings
import util.get
import webcore.materialUI.*
import webcore.mbMaterialDialog

interface AccessManagementTableRowProps : RProps {
  class Config(
    val accessManagement: ClientAccessManagement,
    val onEditFinished: (response: String?) -> Unit
  )

  var config: Config
  var classes: AccessManagementTableRowClasses
}

interface AccessManagementTableRowState : RState {
  var showEditAccessManagementDialog: Boolean
  var working: Boolean
}

class AccessManagementTableRow : RComponent<AccessManagementTableRowProps, AccessManagementTableRowState>() {

  override fun AccessManagementTableRowState.init() {
    showEditAccessManagementDialog = false
    working = false
  }

  private fun RBuilder.renderEditAccessManagementDialog() = mbMaterialDialog(
    show = true,
    title = Strings.location_edit.get(),
    customContent = {
      // TODO: Access management edit dialog
//      renderAddLocation(AddLocationProps.Config.Edit(props.config.location, onFinished = { response ->
//        if (response == "ok") {
//          setState {
//            showEditAccessManagementDialog = false
//          }
//        }
//        props.config.onEditFinished(response)
//      }))
    },
    buttons = null,
    onClose = {
      setState {
        showEditAccessManagementDialog = false
      }
    }
  )

  override fun RBuilder.render() {
    if (state.showEditAccessManagementDialog) {
      renderEditAccessManagementDialog()
    }
    mTableRow {
      attrs.hover = true
      // TODO: Click to see the details/edit

      mTableCell {
        +props.config.accessManagement.locationName
      }
      mTableCell {
        +props.config.accessManagement.allowedEmails.count().toString()
      }
      mTableCell {
        +props.config.accessManagement.note
      }
      mTableCell {
        if (state.working) {
          circularProgress {}
        } else {
          materialMenu(
            menuItems = listOf(
              MenuItem(text = Strings.edit.get(), icon = editIcon, onClick = {
              }),
              MenuItem(text = Strings.delete.get(), icon = deleteIcon, onClick = {
              }),
              MenuItem(text = Strings.copy.get(), icon = fileCopyOutlinedIcon, onClick = {
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
