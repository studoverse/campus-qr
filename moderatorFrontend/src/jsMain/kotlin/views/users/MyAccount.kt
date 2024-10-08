package views.users

import app.AppContext
import app.appContextToInject
import web.cssom.*
import mui.material.Box
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.ToolbarViewConfig
import views.common.ToolbarViewFc
import webcore.RComponent

external interface MyAccountProps : Props {
}

external interface MyAccountState : State

private class MyAccount : RComponent<MyAccountProps, MyAccountState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(MyAccount::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun ChildrenBuilder.render() {
    val userData = appContext.userDataContext.userData!!
    ToolbarViewFc {
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
      AddUserFc {
        config = AddUserConfig.Edit(
          userData.clientUser!!,
          onFinished = {},
        )
      }
    }
  }
}

fun ChildrenBuilder.renderMyAccount() {
  MyAccount::class.react {}
}
