package views.users

import app.appContextToInject
import web.cssom.*
import web.window.window
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML
import util.Strings
import util.get
import views.common.*
import web.window.WindowTarget
import webcore.*

external interface ListUsersProps : Props

val ListUsers = FcWithCoroutineScope<ListUsersProps> { props, launch ->
  val listUsersController = ListUsersController.useListUsersController(
    launch = launch,
  )

  val dialogRef = useRef<MbDialogRef>()
  val appContext = useContext(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!

  fun renderAddUserDialog() = dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.user_add.get()),
      customContent = {
        AddUserFc {
          config = AddUserConfig.Create(onFinished = { response ->
            listUsersController.handleCreateOrAddUserResponse(response)
            dialogRef.current!!.closeDialog()
          })
        }
      },
    )
  )


  fun renderSsoInfoButtonDialog() {
    dialogRef.current!!.showDialog(
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

  MbDialogFc { ref = dialogRef }
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

  if (userData.externalAuthProvider) {
    Box {
      sx {
        backgroundColor = rgb(232, 244, 253)
        borderRadius = 4.px
        marginLeft = 16.px
        marginRight = 16.px
        marginBottom = 16.px
        padding = 16.px
      }
      +Strings.user_administration_external_auth_provider.get()
    }
  }

  MbLinearProgressFc { show = listUsersController.loadingUserList }

  if (listUsersController.userList?.isNotEmpty() == true) {
    Table {
      TableHead {
        TableRow {
          TableCell { +Strings.user_name.get() }
          TableCell { +Strings.email_address.get() }
          TableCell { +Strings.user_permissions.get() }
          TableCell { +Strings.user_first_login_date.get() }
          TableCell { +Strings.actions.get() }
        }
      }
      TableBody {
        listUsersController.userList!!.forEach { user ->
          renderUserTableRow(
            config = UserTableRowConfig(
              user = user,
              dialogRef = dialogRef,
              onEditFinished = listUsersController.handleCreateOrAddUserResponse,
            ),
          )
        }
      }
    }
  } else if (listUsersController.userList == null && !listUsersController.loadingUserList) {
    networkErrorView()
  } else if (!listUsersController.loadingUserList) {
    throw Exception("At least one user must exist")
  }
}