package views.users.listUsers

import app.appContextToInject
import web.cssom.*
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.*
import views.users.UserTableRow
import views.users.UserTableRowConfig
import webcore.*

external interface ListUsersProps : Props

val ListUsers = FcWithCoroutineScope<ListUsersProps> { props, launch ->
  val listUsersController = ListUsersController.Companion.useListUsersController(
    launch = launch,
  )

  val dialogRef = useRef<MbDialogRef>()
  val appContext = useContext(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!

  MbDialogFc { ref = dialogRef }
  ListUsersToolbarView {
    config = ListUsersToolbarViewConfig(
      dialogRef = dialogRef,
      handleCreateOrAddUserResponse = listUsersController.handleCreateOrAddUserResponse,
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
        listUsersController.userList.forEach { user ->
          UserTableRow {
            config = UserTableRowConfig(
              user = user,
              dialogRef = dialogRef,
              onEditFinished = listUsersController.handleCreateOrAddUserResponse,
            )
          }
        }
      }
    }
  } else if (listUsersController.userList == null && !listUsersController.loadingUserList) {
    networkErrorView()
  } else if (!listUsersController.loadingUserList) {
    throw Exception("At least one user must exist")
  }
}
