package views.users

import app.appContextToInject
import js.lazy.Lazy
import web.cssom.*
import mui.material.Box
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.ToolbarView.ToolbarViewConfig
import views.common.ToolbarView.ToolbarView
import views.users.addUser.AddUserConfig
import views.users.addUser.AddUser
import webcore.FcWithCoroutineScope

external interface MyAccountProps : Props {}

@Lazy
val MyAccount = FcWithCoroutineScope<MyAccountProps> { props, launch ->
  val appContext = use(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!
  Suspense {
    ToolbarView {
      config = ToolbarViewConfig(
        title = Strings.account_settings.get(),
        buttons = emptyList()
      )
    }
  }
  Box {
    sx {
      marginTop = 32.px
      marginLeft = 32.px
      marginRight = marginLeft
      marginBottom = 32.px
    }
    Suspense {
      AddUser {
        config = AddUserConfig.Edit(
          userData.clientUser!!,
          onFinished = {},
        )
      }
    }
  }
}