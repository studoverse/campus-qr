package views.users.listUsers

import app.appContextToInject
import js.lazy.Lazy
import web.cssom.*
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.*
import views.users.userTableRow.UserTableRow
import views.users.userTableRow.UserTableRowConfig
import views.users.listUsersToolbarView.ListUsersToolbarView
import views.users.listUsersToolbarView.ListUsersToolbarViewConfig
import webcore.*

external interface ListUsersProps : Props

@Lazy
val ListUsers = FcWithCoroutineScope<ListUsersProps> { props, launch ->
  val controller = ListUsersController.use(
    launch = launch,
  )

  val dialogRef = useRef<MbDialogRef>()
  val appContext = use(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!

  MbDialog { ref = dialogRef }
  Suspense {
    ListUsersToolbarView {
      config = ListUsersToolbarViewConfig(
        dialogRef = dialogRef,
        handleCreateOrAddUserResponse = controller.handleCreateOrAddUserResponse,
      )
    }
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

  MbLinearProgress { show = controller.loadingUserList }

  if (controller.userList?.isNotEmpty() == true) {
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
        Suspense {
          controller.userList.forEach { user ->
            UserTableRow {
              key = user.id
              config = UserTableRowConfig(
                user = user,
                dialogRef = dialogRef,
                onEditFinished = controller.handleCreateOrAddUserResponse,
              )
            }
          }
        }
      }
    }
  } else if (controller.userList == null && !controller.loadingUserList) {
    networkErrorView()
  } else if (!controller.loadingUserList) {
    throw Exception("At least one user must exist")
  }
}
