package views.users.userTableRow

import js.lazy.Lazy
import app.appContextToInject
import com.studo.campusqr.common.payloads.DeleteUserData
import web.cssom.*
import mui.icons.material.Delete
import mui.icons.material.Edit
import mui.material.Box
import mui.material.TableCell
import mui.material.TableRow
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ul
import util.Strings
import util.apiBase
import util.get
import util.localizedString
import views.users.addUser.AddUserConfig
import views.users.addUser.AddUser
import web.prompts.confirm
import webcore.*
import webcore.materialMenu.MaterialMenu
import webcore.materialMenu.MaterialMenuConfig
import webcore.materialMenu.MaterialMenuConfig.Companion.MenuItem

external interface UserTableRowProps : Props {
  var config: UserTableRowConfig
}

@Lazy
val UserTableRow = FcWithCoroutineScope<UserTableRowProps> { props, launch ->
  fun renderEditUserDialog() = props.config.dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.user_edit.get()),
      customContent = {
        Suspense {
          AddUser {
            config = AddUserConfig.Edit(
              user = props.config.user,
              onFinished = { response ->
                props.config.onEditFinished(response)
                props.config.dialogRef.current!!.closeDialog()
              }
            )
          }
        }
      },
    )
  )

  val appContext = use(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!
  TableRow {
    TableCell {
      +props.config.user.name
    }
    TableCell {
      +props.config.user.email
    }
    TableCell {
      Box {
        component = ul
        sx {
          margin = 0.px
          paddingInlineStart = 20.px
        }
        props.config.user.permissions.map { permission ->
          li {
            key = permission.name
            +permission.localizedString.get()
          }
        }
      }
    }
    TableCell {
      +props.config.user.firstLoginDate
    }
    TableCell {
      Suspense {
        MaterialMenu {
          config = MaterialMenuConfig(
            menuItems = listOf(
              MenuItem(text = Strings.user_edit.get(), icon = Edit, onClick = {
                renderEditUserDialog()
              }),
              MenuItem(text = Strings.user_delete.get(), icon = Delete, onClick = {
                if (confirm(Strings.user_delete_are_you_sure.get())) {
                  launch {
                    val response = NetworkManager.post<String>(
                      "$apiBase/user/delete",
                      body = DeleteUserData(userId = props.config.user.id)
                    )
                    props.config.onEditFinished(response)
                  }
                }
              }, enabled = props.config.user.id != userData.clientUser!!.id), // Don't delete own user for better UX
            )
          )
        }
      }
    }
  }
}
