package views.users

import com.studo.campusqr.common.payloads.UserData
import csstype.px
import kotlinx.js.jso
import mui.material.Box
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.get
import views.common.ToolbarViewConfig
import views.common.renderToolbarView
import webcore.MbSnackbarConfig
import webcore.RComponent
import webcore.mbSnackbar
import webcore.setState

class MyAccountConfig(val userData: UserData)

external interface MyAccountProps : Props {
  var config: MyAccountConfig
}

external interface MyAccountState : State {
  var snackbarText: String
}

class MyAccount : RComponent<MyAccountProps, MyAccountState>() {

  override fun MyAccountState.init() {
    snackbarText = ""
  }

  override fun ChildrenBuilder.render() {
    renderToolbarView {
      config = ToolbarViewConfig(
        title = Strings.account_settings.get(),
        buttons = emptyList()
      )
    }
    Box {
      sx {
        marginTop = 32.px
        marginLeft = 32.px
        marginRight = marginLeft
        marginBottom = 32.px
      }
      mbSnackbar {
        config = MbSnackbarConfig(
          show = state.snackbarText.isNotEmpty(),
          message = state.snackbarText,
          onClose = {
            setState {
              snackbarText = ""
            }
          }
        )
      }
      renderAddUser {
        config = AddUserConfig.Edit(
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
        )
        userData = props.config.userData
      }
    }
  }
}

fun ChildrenBuilder.renderMyAccount(handler: MyAccountProps.() -> Unit) {
  MyAccount::class.react {
    +jso(handler)
  }
}
