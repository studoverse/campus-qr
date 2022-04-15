package views.users

import app.AppContext
import app.appContext
import csstype.px
import mui.material.Box
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.ToolbarViewConfig
import views.common.renderToolbarView
import webcore.MbSnackbarConfig
import webcore.RComponent
import webcore.mbSnackbar
import webcore.setState

external interface MyAccountProps : Props {
}

external interface MyAccountState : State {
  var snackbarText: String
}

private class MyAccount : RComponent<MyAccountProps, MyAccountState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(MyAccount::class) {
    init {
      this.contextType = appContext
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun MyAccountState.init() {
    snackbarText = ""
  }

  override fun ChildrenBuilder.render() {
    val userData = appContext.userDataContext.userData!!
    renderToolbarView(
      config = ToolbarViewConfig(
        title = Strings.account_settings.get(),
        buttons = emptyList()
      )
    )
    Box {
      sx {
        marginTop = 32.px
        marginLeft = 32.px
        marginRight = marginLeft
        marginBottom = 32.px
      }
      mbSnackbar(
        config = MbSnackbarConfig(
          show = state.snackbarText.isNotEmpty(),
          message = state.snackbarText,
          onClose = {
            setState {
              snackbarText = ""
            }
          }
        )
      )
      renderAddUser(
        config = AddUserConfig.Edit(
          userData.clientUser!!,
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
      )
    }
  }
}

fun ChildrenBuilder.renderMyAccount() {
  MyAccount::class.react {}
}
