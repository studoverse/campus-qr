package views.users

import app.appContextToInject
import web.cssom.*
import mui.material.Box
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.ToolbarViewConfig
import views.common.ToolbarViewFc
import webcore.FcWithCoroutineScope

external interface MyAccountProps : Props {}

val MyAccountFc = FcWithCoroutineScope<MyAccountProps> { props, launch ->
  val appContext = useContext(appContextToInject)!!
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