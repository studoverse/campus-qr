package views.accessManagement

import app.GlobalCss
import kotlinext.js.js
import react.*
import react.dom.div
import util.Strings
import util.get
import views.accessManagement.AccessManagementDetailsProps.*
import webcore.materialUI.muiButton
import webcore.materialUI.typography
import webcore.materialUI.withStyles

interface AccessManagementDetailsProps : RProps {
  sealed class Config {
    class Create : Config()
    class Edit : Config()
    class Details(val id: String) : Config()
  }

  var config: Config
  var classes: AccessManagementDetailsClasses
}

interface AccessManagementDetailsState : RState

class AccessManagementDetails : RComponent<AccessManagementDetailsProps, AccessManagementDetailsState>() {
  override fun RBuilder.render() {
    div(GlobalCss.flex) {
      typography {
        attrs.className = props.classes.header
        attrs.variant = "h5"
        +Strings.access_control.get()
      }
      div(GlobalCss.flexEnd) {

        if (props.config !is Config.Details) {
          muiButton {
            attrs.classes = kotlinext.js.js {
              root = props.classes.button
            }
            attrs.variant = "contained"
            attrs.color = "primary"
            attrs.onClick = {

            }
            +when (props.config) {
              is Config.Create -> "Create"
              is Config.Edit -> "Save"
              else -> error("Unknown config: ${props.config}")
            }
          }
        }
      }
    }
  }
}

interface AccessManagementDetailsClasses {
  // Keep in sync with CreateAccessManagementStyle!
  var header: String
  var button: String
}

private val AccessManagementDetailsStyle = { theme: dynamic ->
  // Keep in sync with CreateAccessManagementClasses!
  js {
    header = js {
      margin = 16
    }
    button = js {
      marginRight = 16
      marginTop = 16
      marginBottom = 16
      marginLeft = 8
    }
  }
}

private val styled = withStyles<AccessManagementDetailsProps, AccessManagementDetails>(AccessManagementDetailsStyle)

fun RBuilder.renderAccessManagementDetails(config: Config) = styled {
  attrs.config = config
}
