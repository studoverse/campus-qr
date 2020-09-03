package views.users

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.ClientUser
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import react.dom.br
import react.dom.div
import util.Strings
import util.get
import views.common.networkErrorView
import views.common.spacer
import webcore.*
import webcore.extensions.launch
import webcore.materialUI.*

interface ListUsersProps : RProps {
  var currentUser: ClientUser
  var classes: ListUsersClasses
}

interface ListUsersState : RState {
  var userList: List<ClientUser>
  var showAddUserDialog: Boolean
  var showSsoInfoDialog: Boolean
  var loadingUserList: Boolean
  var snackbarText: String
}

class ListUsers : RComponent<ListUsersProps, ListUsersState>() {

  override fun ListUsersState.init() {
    userList = emptyList()
    showAddUserDialog = false
    showSsoInfoDialog = false
    loadingUserList = false
    snackbarText = ""
  }

  private fun fetchUserList() = launch {
    setState { loadingUserList = true }
    val response = NetworkManager.get<Array<ClientUser>>("$apiBase/user/list")
    setState {
      userList = response?.toList() ?: emptyList()
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
        config = AddUserProps.Config.Create(onFinished = { response -> handleCreateOrAddUserResponse(response) }),
        currentUser = props.currentUser
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
    MbSnackbarProps.Config(show = state.snackbarText.isNotEmpty(), message = state.snackbarText, onClose = {
      setState { snackbarText = "" }
    })
  )

  override fun RBuilder.render() {
    renderAddUserDialog()
    renderSsoInfoButtonDialog()
    renderSnackbar()

    div(GlobalCss.flex) {
      typography {
        attrs.className = props.classes.header
        attrs.variant = "h5"
        +Strings.user_management.get()
      }
      div(GlobalCss.flexEnd) {
        muiButton {
          attrs.classes = js {
            root = props.classes.importButton
          }
          attrs.variant = "outlined"
          attrs.color = "primary"
          attrs.onClick = {
            setState {
              showSsoInfoDialog = true
            }
          }
          +Strings.user_sso_info.get()
        }

        muiButton {
          attrs.classes = js {
            root = props.classes.createButton
          }
          attrs.variant = "contained"
          attrs.color = "primary"
          attrs.onClick = {
            setState {
              showAddUserDialog = true
            }
          }
          +Strings.user_add.get()
        }
      }
    }

    typography {
      attrs.className = props.classes.subtitle
      attrs.variant = "subtitle1"
      +Strings.user_administration_hint1.get()
      br { }
      +Strings.user_administration_hint2.get()
    }

    div(props.classes.progressHolder) {
      if (state.loadingUserList) {
        linearProgress {}
      }
    }

    if (state.userList.isNotEmpty()) {
      mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.user_name.get() }
            mTableCell { +Strings.email_address.get() }
            mTableCell { +Strings.user_permission.get() }
            mTableCell { +Strings.user_first_login_date.get() }
            mTableCell { +Strings.actions.get() }
          }
        }
        mTableBody {
          state.userList.forEach { user ->
            renderUserTableRow(
              UserTableRowProps.Config(user, onEditFinished = { response ->
                handleCreateOrAddUserResponse(response)
              }),
              currentUser = props.currentUser
            )
          }
        }
      }
    } else if (!state.loadingUserList) {
      networkErrorView()
    }
  }
}

interface ListUsersClasses {
  var header: String
  var subtitle: String
  var importButton: String
  var createButton: String
  var progressHolder: String
  var dialogContent: String
  // Keep in sync with ListUsersStyle!
}

private val ListUsersStyle = { theme: dynamic ->
  // Keep in sync with ListUsersClasses!
  js {
    header = js {
      margin = 16
    }
    subtitle = js {
      margin = 16
    }
    importButton = js {
      margin = 16
    }
    createButton = js {
      margin = 16
    }
    progressHolder = js {
      height = 8
    }
    dialogContent = js {
      color = "rgba(0, 0, 0, 0.54)"
    }
  }
}

private val styled = withStyles<ListUsersProps, ListUsers>(ListUsersStyle)

fun RBuilder.renderUsers(currentUser: ClientUser) = styled {
  attrs.currentUser = currentUser
}
  