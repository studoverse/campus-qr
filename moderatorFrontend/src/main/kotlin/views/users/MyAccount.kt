package views.users

import com.studo.campusqr.common.payloads.UserData
import kotlinext.js.js
import react.*
import react.dom.div
import util.Strings
import util.get
import views.common.ToolbarViewProps
import views.common.renderToolbarView
import webcore.MbSnackbarProps
import webcore.materialUI.withStyles
import webcore.mbSnackbar

interface MyAccountProps : RProps {
  var classes: MyAccountClasses
  var config: Config

  class Config(val userData: UserData)
}

interface MyAccountState : RState {
  var snackbarText: String
}

class MyAccount : RComponent<MyAccountProps, MyAccountState>() {

  override fun MyAccountState.init() {
    snackbarText = ""
  }

  override fun RBuilder.render() {
    renderToolbarView(
      ToolbarViewProps.Config(
        title = Strings.account_settings.get(),
        buttons = emptyList()
      )
    )
    div(classes = props.classes.container) {
      mbSnackbar(
        MbSnackbarProps.Config(
          show = state.snackbarText.isNotEmpty(),
          message = state.snackbarText,
          onClose = {
            setState {
              snackbarText = ""
            }
          })
      )
      renderAddUser(
        config = AddUserProps.Config.Edit(
          props.config.userData.clientUser!!,
          onFinished = { result ->
            setState {
              snackbarText = if (result == "ok") {
                Strings.user_updated_account_details.get()
              } else {
                Strings.network_error.get()
              }
            }
          }
        ),
        userData = props.config.userData
      )
    }
  }
}

interface MyAccountClasses {
  var container: String
}

private val style = { _: dynamic ->
  js {
    container = js {
      marginTop = 32
      marginLeft = 32
      marginRight = marginLeft
      marginBottom = 32
    }
  }
}

private val styled = withStyles<MyAccountProps, MyAccount>(style)

fun RBuilder.renderMyAccount(config: MyAccountProps.Config) = styled {
  // Set component attrs here
  attrs.config = config
}
  