package views.users.listUsers

import app.appContextToInject
import js.lazy.Lazy
import mui.material.ButtonVariant
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.MutableRefObject
import react.Props
import react.Suspense
import react.dom.html.ReactHTML
import react.useContext
import util.Strings
import util.get
import views.common.ToolbarButton
import views.common.ToolbarViewConfig
import views.common.spacer
import views.common.ToolbarViewFc
import views.users.AddUserConfig
import views.users.AddUserFc
import web.cssom.rgb
import web.window.WindowTarget
import web.window.window
import webcore.DialogConfig
import webcore.FcWithCoroutineScope
import webcore.MbDialogRef
import webcore.DialogButton

class ListUsersToolbarViewConfig(
  val dialogRef: MutableRefObject<MbDialogRef>,
  val handleCreateOrAddUserResponse: (String?) -> Unit,
)

external interface ListUsersToolbarViewProps : Props {
  var config: ListUsersToolbarViewConfig
}

//@Lazy
val ListUsersToolbarView = FcWithCoroutineScope<ListUsersToolbarViewProps> { props, launch ->
  val appContext = useContext(appContextToInject)!!
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
          AddUserFc {
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
    ToolbarViewFc {
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