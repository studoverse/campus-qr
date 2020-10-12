package views.guestAccessManagement.guestAccessManagementOverview

import MenuItem
import com.studo.campusqr.common.ActiveCheckIn
import kotlinx.browser.window
import materialMenu
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import util.Strings
import util.get
import webcore.materialUI.*

interface GuestAccessManagementRowProps : RProps {
  var classes: QuestAccessManagementRowClasses
  var config: Config
  class Config(
    val activeCheckIn: ActiveCheckIn
  )
}

interface GuestAccessManagementRowState : RState

class GuestAccessManagementRow : RComponent<GuestAccessManagementRowProps, GuestAccessManagementRowState>() {
  override fun RBuilder.render() {
    mTableRow {
      mTableCell {
        +props.config.activeCheckIn.locationName
      }
      mTableCell {
        +props.config.activeCheckIn.email
      }
      mTableCell {
        +(props.config.activeCheckIn.seat?.toString() ?: "-")
      }
      mTableCell {
        materialMenu(
          menuItems = listOf(
            MenuItem(text = "Check-out", icon = checkIcon, onClick = {
              TODO("Not implemented")
            }),
            MenuItem(text = Strings.delete.get(), icon = deleteIcon, onClick = {
              if (window.confirm(Strings.access_control_delete_are_your_sure.get())) {
                TODO("Not implemented")
              }
            }),
          )
        )
      }
    }
  }
}

interface QuestAccessManagementRowClasses {
  // Keep in sync with QuestAccessManagementRowStyle!
}

private val QuestAccessManagementRowStyle = { theme: dynamic ->
  // Keep in sync with QuestAccessManagementRowClasses!
}

private val styled = withStyles<GuestAccessManagementRowProps, GuestAccessManagementRow>(QuestAccessManagementRowStyle)

fun RBuilder.renderGuestAccessManagementRow(config: GuestAccessManagementRowProps.Config) = styled {
  attrs.config = config
}
  