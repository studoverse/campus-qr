package views.users

import apiBase
import app.GlobalCss
import com.studo.campusqr.common.*
import com.studo.campusqr.common.extensions.emailRegex
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import kotlinext.js.js
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import util.Strings
import util.get
import util.localizedString
import views.common.spacer
import views.users.AddUserProps.Config
import webcore.NetworkManager
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.materialUI.*

interface AddUserProps : RProps {
  sealed class Config(val onFinished: (response: String?) -> Unit) {
    class Create(onFinished: (response: String?) -> Unit) : Config(onFinished)
    class Edit(val user: ClientUser, onFinished: (response: String?) -> Unit) : Config(onFinished)
  }

  var config: Config
  var userData: UserData
  var classes: AddUserClasses
}

interface AddUserState : RState {
  var userCreationInProgress: Boolean

  var userEmailTextFieldValue: String
  var userEmailTextFieldError: String

  var userNameTextFieldValue: String
  var userNameTextFieldError: String

  var userPasswordTextFieldValue: String
  var userPasswordTextFieldError: String

  var userPermissions: Set<UserPermission>
}

class AddUser(props: AddUserProps) : RComponent<AddUserProps, AddUserState>(props) {

  override fun AddUserState.init(props: AddUserProps) {
    userCreationInProgress = false

    userEmailTextFieldValue = (props.config as? Config.Edit)?.user?.email ?: ""
    userEmailTextFieldError = ""

    userPasswordTextFieldValue = ""
    userPasswordTextFieldError = ""

    userNameTextFieldValue = (props.config as? Config.Edit)?.user?.name ?: ""
    userNameTextFieldError = ""

    userPermissions = (props.config as? Config.Edit)?.user?.permissions ?: setOf(UserPermission.EDIT_OWN_ACCESS)
  }

  private fun createNewUser() = launch {
    setState { userCreationInProgress = true }
    val response = NetworkManager.post<String>(
      url = "$apiBase/user/create",
      json = JSON.stringify(
        NewUserData(
          email = state.userEmailTextFieldValue,
          password = state.userPasswordTextFieldValue,
          name = state.userNameTextFieldValue,
          permissions = state.userPermissions.map { it.name }.toTypedArray()
        )
      )
    )
    setState {
      userCreationInProgress = false
    }
    props.config.onFinished(response)
  }

  private fun editUser() = launch {
    setState { userCreationInProgress = true }
    val response = NetworkManager.post<String>(
      url = "$apiBase/user/edit",
      json = JSON.stringify(
        EditUserData(
          userId = (props.config as Config.Edit).user.id,
          name = state.userNameTextFieldValue.emptyToNull(),
          password = state.userPasswordTextFieldValue.emptyToNull(),
          permissions = state.userPermissions
            .takeIf { (props.config as Config.Edit).user.permissions != it }
            ?.map { it.name }
            ?.toTypedArray()
        )
      )
    )
    setState {
      userCreationInProgress = false
    }
    props.config.onFinished(response)
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

  override fun RBuilder.render() {
    textField {
      attrs.error = state.userEmailTextFieldError.isNotEmpty()
      attrs.helperText = state.userEmailTextFieldError
      attrs.fullWidth = true
      attrs.variant = "outlined"
      attrs.value = state.userEmailTextFieldValue
      attrs.autoComplete = "username"
      attrs.label = Strings.email_address.get()
      attrs.type = "email"
      if (props.config is Config.Edit) {
        attrs.disabled = true
      }
      attrs.onChange = { event: Event ->
        val value = event.inputValue
        setState {
          userEmailTextFieldValue = value
          userEmailTextFieldError = ""
        }
      }
    }

    spacer(16)

    if (!props.userData.externalAuthProvider) {
      textField {
        attrs.error = state.userPasswordTextFieldError.isNotEmpty()
        attrs.helperText = state.userPasswordTextFieldError
        attrs.fullWidth = true
        attrs.type = "password"
        attrs.variant = "outlined"
        if (props.config is Config.Create) {
          attrs.label = Strings.login_email_form_pw_label.get()
        } else {
          attrs.label = Strings.login_email_form_new_pw_label.get()
          attrs.autoComplete = "new-password"
        }
        attrs.value = state.userPasswordTextFieldValue
        attrs.onChange = { event: Event ->
          val value = event.inputValue
          setState {
            userPasswordTextFieldValue = value
            userPasswordTextFieldError = ""
          }
        }
      }
      spacer(16)
    }

    textField {
      attrs.error = state.userNameTextFieldError.isNotEmpty()
      attrs.helperText = state.userNameTextFieldError
      attrs.fullWidth = true
      attrs.variant = "outlined"
      attrs.label = Strings.user_name.get()
      attrs.value = state.userNameTextFieldValue
      attrs.autoComplete = "off"
      attrs.onChange = { event: Event ->
        val value = event.inputValue
        setState {
          userNameTextFieldValue = value
          userNameTextFieldError = ""
        }
      }
    }

    spacer(16)

    // This view is is either used for user management, or to change own user properties
    if (props.userData.clientUser!!.canEditUsers) {
      typography {
        +Strings.user_permissions.get()
      }
      div(classes = props.classes.userPermissionsSwitch) {
        formControl {
          attrs.fullWidth = true
          attrs.variant = "outlined"

          UserPermission.values().forEach { userPermission ->
            formControlLabel {
              attrs.control = mCheckbox {
                attrs.checked = userPermission in state.userPermissions
                attrs.onChange = { event, checked ->
                  val existingPermissions = state.userPermissions
                  if (checked) {
                    setState { userPermissions = existingPermissions + userPermission }
                  } else {
                    setState { userPermissions = existingPermissions - userPermission }
                  }
                }
              }
              attrs.label = userPermission.localizedString.get()
            }
          }
        }
      }
    }

    spacer(32)

    div(GlobalCss.flex) {
      div(GlobalCss.flexEnd) {
        muiButton {
          attrs.classes = js {
            root = props.classes.addButton
          }
          attrs.variant = "contained"
          attrs.color = "primary"
          attrs.onClick = {
            when (props.config) {
              is Config.Create -> if (validateNameInput() && validatePasswordInput() && validateEmailInput()) {
                createNewUser()
              }
              is Config.Edit -> editUser()
            }
          }
          +when (props.config) {
            is Config.Create -> Strings.user_add.get()
            is Config.Edit -> Strings.user_update.get()
          }
        }
      }
    }
  }
}

interface AddUserClasses {
  // Keep in sync with AddUserStyle!
  var addButton: String
  var userPermissionsSwitch: String
}

private val AddUserStyle = { theme: dynamic ->
  // Keep in sync with AddUserClasses!
  js {
    addButton = js {
      marginBottom = 16
    }
    userPermissionsSwitch = js {
      display = "flex"
      justifyContent = "center"
      alignItems = "center"
      fontFamily = "'Roboto', Arial, sans-serif"
    }
  }
}

private val styled = withStyles<AddUserProps, AddUser>(AddUserStyle)

fun RBuilder.renderAddUser(config: Config, userData: UserData) = styled {
  attrs.config = config
  attrs.userData = userData
}
  