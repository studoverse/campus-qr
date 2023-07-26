package views.users

import app.AppContext
import app.appContextToInject
import com.studo.campusqr.common.payloads.ClientUser
import web.cssom.*
import web.window.window
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML
import util.Strings
import util.apiBase
import util.get
import views.common.*
import web.window.WindowTarget
import webcore.*
import webcore.extensions.launch

external interface ListUsersProps : Props

external interface ListUsersState : State {
  var userList: List<ClientUser>?
  var loadingUserList: Boolean
}

private class ListUsers : RComponent<ListUsersProps, ListUsersState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(ListUsers::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  private val dialogRef = createRef<MbDialog>()

  override fun ListUsersState.init() {
    userList = null
    loadingUserList = false
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
    val snackbarText = when (response) {
      "already_exists" -> Strings.user_already_exists.get()
      "ok" -> {
        fetchUserList()
        Strings.user_created.get()
      }
      else -> Strings.error_try_again.get()
    }
    appContext.showSnackbar(snackbarText)
  }

  private fun renderAddUserDialog() = dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.user_add.get()),
      customContent = DialogConfig.CustomContent(AddUser::class) {
        config = AddUserConfig.Create(onFinished = { response ->
          handleCreateOrAddUserResponse(response)
          dialogRef.current!!.closeDialog()
        })
      },
    )
  )


  private fun renderSsoInfoButtonDialog() {
    dialogRef.current!!.showDialog(
      DialogConfig(
        title = DialogConfig.Title(text = Strings.user_sso_info.get()),
        customContent = basicCustomContent {
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

  override fun ChildrenBuilder.render() {
    mbDialog(ref = dialogRef)
    val userData = appContext.userDataContext.userData!!
    renderToolbarView(
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
    )

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
              config = UserTableRowConfig(
                user = user,
                dialogRef = dialogRef,
                onEditFinished = { response ->
                  handleCreateOrAddUserResponse(response)
                },
              ),
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

fun ChildrenBuilder.renderUsers() {
  ListUsers::class.react {}
}
