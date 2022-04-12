package views.users

import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.UserData
import csstype.px
import csstype.rgb
import csstype.rgba
import kotlinx.browser.window
import mui.material.*
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.dom.html.ReactHTML
import react.react
import util.Strings
import util.apiBase
import util.get
import views.common.*
import webcore.*
import webcore.extensions.launch

external interface ListUsersProps : Props {
  var userData: UserData
}

external interface ListUsersState : State {
  var userList: List<ClientUser>?
  var showAddUserDialog: Boolean
  var showSsoInfoDialog: Boolean
  var loadingUserList: Boolean
  var snackbarText: String
}

private class ListUsers : RComponent<ListUsersProps, ListUsersState>() {

  override fun ListUsersState.init() {
    userList = null
    showAddUserDialog = false
    showSsoInfoDialog = false
    loadingUserList = false
    snackbarText = ""
  }

  private fun fetchUserList() = launch {
    setState { loadingUserList = true }
    val response = NetworkManager.get<Array<ClientUser>>("$apiBase/user/list")
    setState {
      userList = response?.toList()
      loadingUserList = false
    }
  }

  override fun componentDidMount() {
    fetchUserList()
  }

  private fun handleCreateOrAddUserResponse(response: String?) {
    setState {
      snackbarText = when (response) {
        "already_exists" -> Strings.user_already_exists.get()
        "ok" -> {
          fetchUserList()
          showAddUserDialog = false
          Strings.user_created.get()
        }
        else -> Strings.error_try_again.get()
      }
    }
  }

  private fun ChildrenBuilder.renderAddUserDialog() = mbMaterialDialog(
    config = MbMaterialDialogConfig(
      show = state.showAddUserDialog,
      title = Strings.user_add.get(),
      customContent = {
        renderAddUser(
          config = AddUserConfig.Create(onFinished = { response -> handleCreateOrAddUserResponse(response) }),
          userData = props.userData
        )
      },
      buttons = null,
      onClose = {
        setState {
          showAddUserDialog = false
        }
      }
    )
  )


  private fun ChildrenBuilder.renderSsoInfoButtonDialog() {

    fun closeDialog() {
      setState {
        showSsoInfoDialog = false
      }
    }

    mbMaterialDialog(
      config = MbMaterialDialogConfig(
        show = state.showSsoInfoDialog,
        title = Strings.user_sso_info.get(),
        customContent = {
          Typography {
            sx {
              color = rgba(0, 0, 0, 0.54)
            }
            variant = "body1"
            component = ReactHTML.span
            +Strings.user_sso_info_details1.get()
            spacer(16)
            +Strings.user_sso_info_details2.get()
          }
        },
        buttons = listOf(
          DialogButton(Strings.more_about_studo.get(), onClick = {
            closeDialog()
            window.open("https://studo.com", "_blank")
          }),
          DialogButton("OK", onClick = ::closeDialog)
        ),
        onClose = ::closeDialog
      )
    )
  }

  private fun ChildrenBuilder.renderSnackbar() = mbSnackbar(
    config = MbSnackbarConfig(show = state.snackbarText.isNotEmpty(), message = state.snackbarText, onClose = {
      setState { snackbarText = "" }
    })
  )

  override fun ChildrenBuilder.render() {
    renderAddUserDialog()
    renderSsoInfoButtonDialog()
    renderSnackbar()
    renderToolbarView(
      config = ToolbarViewConfig(
        title = Strings.user_management.get(),
        buttons = listOfNotNull(
          ToolbarButton(
            text = Strings.user_sso_info.get(),
            variant = ButtonVariant.outlined,
            onClick = {
              setState {
                showSsoInfoDialog = true
              }
            }
          ),
          if (props.userData.externalAuthProvider) null else ToolbarButton(
            text = Strings.user_add.get(),
            variant = ButtonVariant.contained,
            onClick = {
              setState {
                showAddUserDialog = true
              }
            }
          )
        )
      )
    )

    if (props.userData.externalAuthProvider) {
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

    renderMbLinearProgress(show = state.loadingUserList)

    if (state.userList?.isNotEmpty() == true) {
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
          state.userList!!.forEach { user ->
            renderUserTableRow(
              config = UserTableRowConfig(user, onEditFinished = { response ->
                handleCreateOrAddUserResponse(response)
              }),
              userData = props.userData
            )
          }
        }
      }
    } else if (state.userList == null && !state.loadingUserList) {
      networkErrorView()
    } else if (!state.loadingUserList) {
      throw Exception("At least one user must exist")
    }
  }
}

fun ChildrenBuilder.renderUsers(userData: UserData) {
  ListUsers::class.react {
    this.userData = userData
  }
}
