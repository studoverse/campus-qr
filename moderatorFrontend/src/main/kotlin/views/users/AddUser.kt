package views.users

import web.cssom.*
import app.AppContext
import app.GlobalCss
import app.appContextToInject
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.extensions.emailRegex
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.EditUserData
import com.studo.campusqr.common.payloads.NewUserData
import com.studo.campusqr.common.payloads.canEditUsers
import csstype.*
import mui.material.*
import mui.system.sx
import react.*
import web.html.InputType
import react.dom.html.ReactHTML.div
import util.Strings
import util.apiBase
import util.get
import util.localizedString
import views.common.renderMbLinearProgress
import views.common.spacer
import webcore.*
import webcore.extensions.launch

sealed class AddUserConfig(val onFinished: (response: String?) -> Unit) {
  class Create(onFinished: (response: String?) -> Unit) : AddUserConfig(onFinished)
  class Edit(val user: ClientUser, onFinished: (response: String?) -> Unit) : AddUserConfig(onFinished)
}

external interface AddUserProps : Props {
  var config: AddUserConfig
}

external interface AddUserState : State {
  var userCreationInProgress: Boolean

  var userEmailTextFieldValue: String
  var userEmailTextFieldError: String

  var userNameTextFieldValue: String
  var userNameTextFieldError: String

  var userPasswordTextFieldValue: String
  var userPasswordTextFieldError: String

  var userPermissions: Set<UserPermission>
}

@Suppress("UPPER_BOUND_VIOLATED") class AddUser(props: AddUserProps) : RComponent<AddUserProps, AddUserState>(props) {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AddUser::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun AddUserState.init(props: AddUserProps) {
    userCreationInProgress = false

    userEmailTextFieldValue = (props.config as? AddUserConfig.Edit)?.user?.email ?: ""
    userEmailTextFieldError = ""

    userPasswordTextFieldValue = ""
    userPasswordTextFieldError = ""

    userNameTextFieldValue = (props.config as? AddUserConfig.Edit)?.user?.name ?: ""
    userNameTextFieldError = ""

    userPermissions = (props.config as? AddUserConfig.Edit)?.user?.permissions ?: setOf(UserPermission.EDIT_OWN_ACCESS)
  }

  private fun createNewUser() = launch {
    setState { userCreationInProgress = true }
    val response = NetworkManager.post<String>(
      url = "$apiBase/user/create",
      body = NewUserData(
        email = state.userEmailTextFieldValue,
        password = state.userPasswordTextFieldValue,
        name = state.userNameTextFieldValue,
        permissions = state.userPermissions.map { it.name }
      )
    )
    setState {
      userCreationInProgress = false
    }
    props.config.onFinished(response)
    val snackbarText = if (response == "ok") {
      Strings.user_created_account_details.get()
    } else {
      Strings.network_error.get()
    }
    appContext.showSnackbar(snackbarText)
  }

  private fun editUser() = launch {
    setState { userCreationInProgress = true }
    val user = (props.config as AddUserConfig.Edit).user
    val response = NetworkManager.post<String>(
      url = "$apiBase/user/edit",
      body = EditUserData(
        userId = user.id,
        name = state.userNameTextFieldValue.emptyToNull(),
        password = state.userPasswordTextFieldValue.emptyToNull(),
        permissions = state.userPermissions
          .takeIf { user.permissions != it }
          ?.map { it.name }
      )
    )
    val userData = appContext.userDataContext.userData!!
    if (response != null && user.id == userData.clientUser?.id) {
      // Only update currently logged-in user
      appContext.userDataContext.fetchNewUserData()
    }
    setState {
      userCreationInProgress = false
    }
    props.config.onFinished(response)
    val snackbarText = if (response == "ok") {
      Strings.user_updated_account_details.get()
    } else {
      Strings.network_error.get()
    }
    appContext.showSnackbar(snackbarText)
  }

  private fun validateNameInput(): Boolean {
    if (state.userNameTextFieldValue.isEmpty()) {
      setState {
        userNameTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.name.get())
      }
      return false
    }
    return true
  }

  private fun validatePasswordInput(): Boolean {
    when {
      state.userPasswordTextFieldValue.count() < 6 -> {
        setState {
          userPasswordTextFieldError =
            Strings.parameter_too_short_format.get().format(Strings.login_email_form_pw_label.get())
        }
        return false
      }
      state.userPasswordTextFieldValue.isEmpty() -> {
        setState {
          userPasswordTextFieldError =
            Strings.parameter_cannot_be_empty_format.get().format(Strings.login_email_form_pw_label.get())
        }
        return false
      }
      else -> return true
    }
  }

  private fun validateEmailInput(): Boolean {
    when {
      state.userEmailTextFieldValue.isEmpty() -> {
        setState {
          userEmailTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.email_address.get())
        }
        return false
      }
      state.userEmailTextFieldValue.count() < 6 -> {
        setState {
          userEmailTextFieldError = Strings.parameter_too_short_format.get().format(Strings.email_address.get())
        }
        return false
      }
      !state.userEmailTextFieldValue.matches(emailRegex) -> {
        setState { userEmailTextFieldError = Strings.login_register_email_not_valid.get() }
        return false
      }

      else -> return true
    }
  }

  override fun ChildrenBuilder.render() {
    renderMbLinearProgress(show = state.userCreationInProgress)

    val userData = appContext.userDataContext.userData!!
    TextField<OutlinedTextFieldProps> {
      error = state.userEmailTextFieldError.isNotEmpty()
      helperText = state.userEmailTextFieldError.toReactNode()
      fullWidth = true
      variant = FormControlVariant.outlined()
      value = state.userEmailTextFieldValue
      autoComplete = "username"
      label = Strings.email_address.get().toReactNode()
      type = InputType.email
      if (props.config is AddUserConfig.Edit) {
        disabled = true
      }
      onChange = { event ->
        val value = event.target.value
        setState {
          userEmailTextFieldValue = value
          userEmailTextFieldError = ""
        }
      }
    }

    spacer(16)

    if (!userData.externalAuthProvider) {
      TextField<OutlinedTextFieldProps> {
        error = state.userPasswordTextFieldError.isNotEmpty()
        helperText = state.userPasswordTextFieldError.toReactNode()
        fullWidth = true
        type = InputType.password
        variant = FormControlVariant.outlined()
        if (props.config is AddUserConfig.Create) {
          label = Strings.login_email_form_pw_label.get().toReactNode()
        } else {
          label = Strings.login_email_form_new_pw_label.get().toReactNode()
          autoComplete = "new-password"
        }
        value = state.userPasswordTextFieldValue
        onChange = { event ->
          val value = event.target.value
          setState {
            userPasswordTextFieldValue = value
            userPasswordTextFieldError = ""
          }
        }
      }
      spacer(16)
    }

    TextField<OutlinedTextFieldProps> {
      error = state.userNameTextFieldError.isNotEmpty()
      helperText = state.userNameTextFieldError.toReactNode()
      fullWidth = true
      variant = FormControlVariant.outlined()
      label = Strings.user_name.get().toReactNode()
      value = state.userNameTextFieldValue
      autoComplete = "off"
      onChange = { event ->
        val value = event.target.value
        setState {
          userNameTextFieldValue = value
          userNameTextFieldError = ""
        }
      }
    }

    spacer(16)

    // This view is is either used for user management, or to change own user properties
    if (userData.clientUser!!.canEditUsers) {
      Typography {
        +Strings.user_permissions.get()
      }
      Box {
        sx {
          display = Display.flex
          justifyContent = JustifyContent.center
          alignItems = AlignItems.center
          fontFamily = string("'Roboto', Arial, sans-serif")
        }
        FormControl {
          fullWidth = true
          variant = FormControlVariant.outlined

          UserPermission.values().forEach { userPermission ->
            FormControlLabel {
              control = Checkbox.create {
                checked = userPermission in state.userPermissions
                onChange = { _, checked ->
                  val existingPermissions = state.userPermissions
                  if (checked) {
                    setState { userPermissions = existingPermissions + userPermission }
                  } else {
                    setState { userPermissions = existingPermissions - userPermission }
                  }
                }
              }
              label = userPermission.localizedString.get().toReactNode()
            }
          }
        }
      }
    }

    spacer(32)

    div {
      className = ClassName(GlobalCss.flex)
      div {
        className = ClassName(GlobalCss.flexEnd)
        Button {
          sx {
            marginBottom = 16.px
          }
          variant = ButtonVariant.contained
          color = ButtonColor.primary
          onClick = {
            when (props.config) {
              is AddUserConfig.Create -> if (validateNameInput() && validatePasswordInput() && validateEmailInput()) {
                createNewUser()
              }
              is AddUserConfig.Edit -> editUser()
            }
          }
          +when (props.config) {
            is AddUserConfig.Create -> Strings.user_add.get()
            is AddUserConfig.Edit -> Strings.user_update.get()
          }
        }
      }
    }
  }
}

fun ChildrenBuilder.renderAddUser(config: AddUserConfig) {
  AddUser::class.react {
    this.config = config
  }
}
