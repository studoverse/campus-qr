package views.users

import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.DeleteUserData
import com.studo.campusqr.common.payloads.UserData
import csstype.px
import kotlinx.browser.window
import kotlinx.js.jso
import mui.icons.material.Delete
import mui.icons.material.Edit
import mui.material.Box
import mui.material.TableCell
import mui.material.TableRow
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ul
import react.react
import util.Strings
import util.apiBase
import util.get
import util.localizedString
import webcore.*
import webcore.extensions.launch

class UserTableRowConfig(
  val user: ClientUser,
  val onEditFinished: (response: String?) -> Unit
)

external interface UserTableRowProps : Props {
  var config: UserTableRowConfig
  var userData: UserData
}

external interface UserTableRowState : State {
  var showEditUserDialog: Boolean
  var snackbarText: String
}

private class UserTableRow : RComponent<UserTableRowProps, UserTableRowState>() {

  override fun UserTableRowState.init() {
    showEditUserDialog = false
    snackbarText = ""
  }

  private fun ChildrenBuilder.renderEditUserDialog() = mbMaterialDialog(handler = {
    config = MbMaterialDialogConfig(
      show = true,
      title = Strings.user_edit.get(),
      customContent = {
        renderAddUser {
          config = AddUserConfig.Edit(props.config.user, onFinished = { response ->
            setState {
              if (response == "ok") {
                showEditUserDialog = false
                snackbarText = Strings.user_updated_account_details.get()
              } else {
                snackbarText = Strings.network_error.get()
              }
            }
            props.config.onEditFinished(response)
          })
          userData = props.userData
        }
      },
      buttons = null,
      onClose = {
        setState {
          showEditUserDialog = false
        }
      }
    )
  }
  )

  override fun ChildrenBuilder.render() {
    mbSnackbar {
      config = MbSnackbarConfig(
        show = state.snackbarText.isNotEmpty(),
        message = state.snackbarText,
        onClose = {
          setState {
            snackbarText = ""
          }
        }
      )
    }
    if (state.showEditUserDialog) {
      renderEditUserDialog()
    }

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
        materialMenu {
          config = MaterialMenuConfig(
            menuItems = listOf(
              MenuItem(text = Strings.user_edit.get(), icon = Edit, onClick = {
                setState {
                  showEditUserDialog = true
                }
              }),
              MenuItem(text = Strings.user_delete.get(), icon = Delete, onClick = {
                if (window.confirm(Strings.user_delete_are_you_sure.get())) {
                  launch {
                    val response = NetworkManager.post<String>(
                      "$apiBase/user/delete",
                      body = DeleteUserData(userId = props.config.user.id)
                    )
                    props.config.onEditFinished(response)
                  }
                }
              }, enabled = props.config.user.id != props.userData.clientUser!!.id), // Don't delete own user for better UX
            )
          )
        }
      }
    }
  }
}

fun ChildrenBuilder.renderUserTableRow(handler: UserTableRowProps.() -> Unit) {
  UserTableRow::class.react {
    +jso(handler)
  }
}
