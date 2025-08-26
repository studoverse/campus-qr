package views.users.listUsersToolbarView

import js.lazy.Lazy
import app.appContextToInject
import mui.material.ButtonVariant
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.Props
import react.Suspense
import react.dom.html.ReactHTML
import react.use
import util.Strings
import util.get
import views.common.ToolbarView.ToolbarButton
import views.common.ToolbarView.ToolbarViewConfig
import views.common.spacer
import views.common.ToolbarView.ToolbarView
import views.users.addUser.AddUserConfig
import views.users.addUser.AddUser
import web.cssom.rgb
import web.window.WindowTarget
import web.window._blank
import web.window.window
import webcore.DialogConfig
import webcore.FcWithCoroutineScope
import webcore.DialogButton

external interface ListUsersToolbarViewProps : Props {
  var config: ListUsersToolbarViewConfig
}

@Lazy
val ListUsersToolbarView = FcWithCoroutineScope<ListUsersToolbarViewProps> { props, launch ->
  val appContext = use(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!

  fun renderSsoInfoButtonDialog() {
    props.config.dialogRef.current!!.showDialog(
      DialogConfig(
        title = DialogConfig.Title(text = Strings.user_sso_info.get()),
        customContent = {
          Typography {
            sx {
              color = rgb(0, 0, 0, 0.54)
            }
            variant = TypographyVariant.body1
            component = ReactHTML.span
            +Strings.user_sso_info_details1.get()
            spacer(16)
            +Strings.user_sso_info_details2.get()
          }
        },
        buttons = listOf(
          DialogButton(Strings.more_about_studo.get(), onClick = {
            window.open("https://studo.com", WindowTarget._blank)
          }),
          DialogButton("OK")
        ),
      )
    )
  }

  fun renderAddUserDialog() = props.config.dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.user_add.get()),
      customContent = {
        Suspense {
          AddUser {
            config = AddUserConfig.Create(onFinished = { response ->
              props.config.handleCreateOrAddUserResponse(response)
              props.config.dialogRef.current!!.closeDialog()
            })
          }
        }
      },
    )
  )

  Suspense {
    ToolbarView {
      config = ToolbarViewConfig(
        title = Strings.user_management.get(),
        buttons = listOfNotNull(
          ToolbarButton(
            text = Strings.user_sso_info.get(),
            variant = ButtonVariant.outlined,
            onClick = {
              renderSsoInfoButtonDialog()
            }
          ),
          if (userData.externalAuthProvider) null else ToolbarButton(
            text = Strings.user_add.get(),
            variant = ButtonVariant.contained,
            onClick = {
              renderAddUserDialog()
            }
          )
        )
      )
    }
  }
}