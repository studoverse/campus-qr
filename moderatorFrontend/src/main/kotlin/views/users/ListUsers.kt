package views.users

import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.UserData
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import react.dom.div
import util.Strings
import util.apiBase
import util.get
import views.common.*
import webcore.*
import webcore.extensions.launch
import webcore.materialUI.*

external interface ListUsersProps : RProps {
  var userData: UserData
  var classes: ListUsersClasses
}

external interface ListUsersState : RState {
  var userList: List<ClientUser>?
  var showAddUserDialog: Boolean
  var showSsoInfoDialog: Boolean
  var loadingUserList: Boolean
  var snackbarText: String
}

class ListUsers : RComponent<ListUsersProps, ListUsersState>() {

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

  private fun RBuilder.renderAddUserDialog() = mbMaterialDialog(
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


  private fun RBuilder.renderSsoInfoButtonDialog(): ReactElement {

    fun closeDialog() {
      setState {
        showSsoInfoDialog = false
      }
    }

    return mbMaterialDialog(
      show = state.showSsoInfoDialog,
      title = Strings.user_sso_info.get(),
      customContent = {
        typography {
          attrs.className = props.classes.dialogContent
          attrs.variant = "body1"
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
  }

  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarConfig(show = state.snackbarText.isNotEmpty(), message = state.snackbarText, onClose = {
      setState { snackbarText = "" }
    })
  )

  override fun RBuilder.render() {
    renderAddUserDialog()
    renderSsoInfoButtonDialog()
    renderSnackbar()
    renderToolbarView(
      ToolbarViewConfig(
        title = Strings.user_management.get(),
        buttons = listOfNotNull(
          ToolbarViewButton(
            text = Strings.user_sso_info.get(),
            variant = "outlined",
            onClick = {
              setState {
                showSsoInfoDialog = true
              }
            }
          ),
          if (props.userData.externalAuthProvider) null else ToolbarViewButton(
            text = Strings.user_add.get(),
            variant = "contained",
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
      div(classes = props.classes.info) {
        +Strings.user_administration_external_auth_provider.get()
      }
    }

    renderLinearProgress(state.loadingUserList)

    if (state.userList?.isNotEmpty() == true) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.user_name.get() }
            mTableCell { +Strings.email_address.get() }
            mTableCell { +Strings.user_permissions.get() }
            mTableCell { +Strings.user_first_login_date.get() }
            mTableCell { +Strings.actions.get() }
          }
        }
        mTableBody {
          state.userList!!.forEach { user ->
            renderUserTableRow(
              UserTableRowConfig(user, onEditFinished = { response ->
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

external interface ListUsersClasses {
  var dialogContent: String
  var info: String
}

private val style = { _: dynamic ->
  js {
    dialogContent = js {
      color = "rgba(0, 0, 0, 0.54)"
    }
    info = js {
      backgroundColor = "rgb(232, 244, 253)"
      borderRadius = 4
      marginLeft = 16
      marginRight = 16
      marginBottom = 16
      padding = 16
    }
  }
}

private val styled = withStyles<ListUsersProps, ListUsers>(style)

fun RBuilder.renderUsers(userData: UserData) = styled {
  attrs.userData = userData
}
  