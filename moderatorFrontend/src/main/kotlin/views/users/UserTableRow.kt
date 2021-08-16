package views.users

import com.studo.campusqr.common.ClientUser
import com.studo.campusqr.common.UserData
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import react.dom.li
import react.dom.ul
import util.Strings
import util.apiBase
import util.get
import util.localizedString
import webcore.*
import webcore.extensions.launch
import webcore.materialUI.*

interface UserTableRowProps : RProps {
  class Config(
    val user: ClientUser,
    val onEditFinished: (response: String?) -> Unit
  )

  var config: Config
  var userData: UserData
  var classes: UserTableRowClasses
}

interface UserTableRowState : RState {
  var showEditUserDialog: Boolean
  var snackbarText: String
}

class UserTableRow : RComponent<UserTableRowProps, UserTableRowState>() {

  override fun UserTableRowState.init() {
    showEditUserDialog = false
    snackbarText = ""
  }

  private fun RBuilder.renderEditUserDialog() = mbMaterialDialog(
    show = true,
    title = Strings.user_edit.get(),
    customContent = {
      renderAddUser(
        AddUserProps.Config.Edit(props.config.user, onFinished = { response ->
          setState {
            if (response == "ok") {
              showEditUserDialog = false
              snackbarText = Strings.user_updated_account_details.get()
            } else {
              snackbarText = Strings.network_error.get()
            }
          }
          props.config.onEditFinished(response)
        }),
        userData = props.userData
      )
    },
    buttons = null,
    onClose = {
      setState {
        showEditUserDialog = false
      }
    }
  )

  override fun RBuilder.render() {
    mbSnackbar(
      MbSnackbarProps.Config(
        show = state.snackbarText.isNotEmpty(),
        message = state.snackbarText,
        onClose = {
          setState {
            snackbarText = ""
          }
        })
    )
    if (state.showEditUserDialog) {
      renderEditUserDialog()
    }

    mTableRow {
      mTableCell {
        +props.config.user.name
      }
      mTableCell {
        +props.config.user.email
      }
      mTableCell {
        ul(classes = props.classes.permissionsList) {
          props.config.user.permissions.map { permission ->
            li {
              +permission.localizedString.get()
            }
          }
        }
      }
      mTableCell {
        +props.config.user.firstLoginDate
      }
      mTableCell {
        materialMenu(
          menuItems = listOf(
            MenuItem(text = Strings.user_edit.get(), icon = editIcon, onClick = {
              setState {
                showEditUserDialog = true
              }
            }),
            MenuItem(text = Strings.user_delete.get(), icon = deleteIcon, onClick = {
              if (window.confirm(Strings.user_delete_are_you_sure.get())) {
                launch {
                  val response = NetworkManager.post<String>(
                    "$apiBase/user/delete",
                    body = mapOf("userId" to props.config.user.id)
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

interface UserTableRowClasses {
  var permissionsList: String
}

private val style = { _: dynamic ->
  js {
    permissionsList = js {
      margin = 0
      paddingInlineStart = "20px"
    }
  }
}

private val styled = withStyles<UserTableRowProps, UserTableRow>(style)

fun RBuilder.renderUserTableRow(config: UserTableRowProps.Config, userData: UserData) = styled {
  attrs.config = config
  attrs.userData = userData
}
  