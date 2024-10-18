package views.users

import app.AppContext
import app.appContextToInject
import com.studo.campusqr.common.payloads.ClientUser
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
import web.prompts.confirm
import webcore.*
import webcore.extensions.launch

class UserTableRowConfig(
  val user: ClientUser,
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onEditFinished: (response: String?) -> Unit
)

external interface UserTableRowProps : Props {
  var config: UserTableRowConfig
}

external interface UserTableRowState : State

private class UserTableRow : RComponent<UserTableRowProps, UserTableRowState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(UserTableRow::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  private fun renderEditUserDialog() = props.config.dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.user_edit.get()),
      customContent = {
        AddUserFc {
          config = AddUserConfig.Edit(
            user = props.config.user,
            onFinished = { response ->
              props.config.onEditFinished(response)
              props.config.dialogRef.current!!.closeDialog()
            }
          )
        }
      },
    )
  )

  override fun ChildrenBuilder.render() {
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
              +permission.localizedString.get()
            }
          }
        }
      }
      TableCell {
        +props.config.user.firstLoginDate
      }
      TableCell {
        materialMenu(
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
        )
      }
    }
  }
}

fun ChildrenBuilder.renderUserTableRow(config: UserTableRowConfig) {
  UserTableRow::class.react {
    this.config = config
  }
}
