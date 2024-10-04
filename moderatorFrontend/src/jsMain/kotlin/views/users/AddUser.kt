package views.users

import web.cssom.*
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
import kotlinx.coroutines.Job
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import util.Strings
import util.apiBase
import util.get
import util.localizedString
import views.common.renderMbLinearProgress
import views.common.spacer
import web.html.InputType
import webcore.*

sealed class AddUserConfig(val onFinished: (response: String?) -> Unit) {
  class Create(onFinished: (response: String?) -> Unit) : AddUserConfig(onFinished)
  class Edit(val user: ClientUser, onFinished: (response: String?) -> Unit) : AddUserConfig(onFinished)
}

external interface AddUserProps : Props {
  var config: AddUserConfig
}

val AddUserFc = FcWithCoroutineScope { props: AddUserProps, launch ->
  var userController = UserController.useUserController(
    user = (props.config as? AddUserConfig.Edit)?.user,
    onFinished = props.config.onFinished,
    launch = launch,
  )

  renderMbLinearProgress(show = userController.userCreationInProgress)

  val appContext = useContext(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!
  TextField {
    error = userController.userEmailTextFieldError.isNotEmpty()
    helperText = userController.userEmailTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    value = userController.userEmailTextFieldValue
    autoComplete = "username"
    label = Strings.email_address.get().toReactNode()
    type = InputType.email
    if (props.config is AddUserConfig.Edit) {
      disabled = true
    }
    onChange = { event ->
      val value = event.target.value
      // TODO: @mh Is there another way to prevent having to pass the setter?
      //  Or is it okay to do it like this?
      userController.setUserEmailTextFieldValue(value)
      userController.setUserEmailTextFieldError("")
    }
  }

  spacer(16)

  if (!userData.externalAuthProvider) {
    TextField {
      error = userController.userPasswordTextFieldError.isNotEmpty()
      helperText = userController.userPasswordTextFieldError.toReactNode()
      fullWidth = true
      type = InputType.password
      variant = FormControlVariant.outlined
      if (props.config is AddUserConfig.Create) {
        label = Strings.login_email_form_pw_label.get().toReactNode()
      } else {
        label = Strings.login_email_form_new_pw_label.get().toReactNode()
        autoComplete = "new-password"
      }
      value = userController.userPasswordTextFieldValue
      onChange = { event ->
        val value = event.target.value
        userController.setUserPasswordTextFieldValue(value)
        userController.setUserPasswordTextFieldError("")
      }
    }
    spacer(16)
  }

  TextField {
    error = userController.userNameTextFieldError.isNotEmpty()
    helperText = userController.userNameTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    label = Strings.user_name.get().toReactNode()
    value = userController.userNameTextFieldValue
    autoComplete = "off"
    onChange = { event ->
      val value = event.target.value
      userController.setUserNameTextFieldValue(value)
      userController.setUserNameTextFieldError("")
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

        UserPermission.entries.forEach { userPermission ->
          FormControlLabel {
            control = Checkbox.create {
              checked = userPermission in userController.userPermissions
              onChange = { _, checked ->
                val existingPermissions = userController.userPermissions
                if (checked) {
                  userController.setUserPermissions(existingPermissions + userPermission)
                } else {
                  userController.setUserPermissions(existingPermissions - userPermission)
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
            is AddUserConfig.Create -> {
              if (userController.validateNameInput() && userController.validatePasswordInput() && userController.validateEmailInput()) {
                userController.createNewUser()
              }
            }

            is AddUserConfig.Edit -> {
              userController.editUser()
            }
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

private data class UserController(
  val createNewUser: () -> Job,
  val editUser: () -> Job,
  val validateNameInput: () -> Boolean,
  val validatePasswordInput: () -> Boolean,
  val validateEmailInput: () -> Boolean,
  val userCreationInProgress: Boolean,
  val userEmailTextFieldValue: String,
  val setUserEmailTextFieldValue: StateSetter<String>,
  val userEmailTextFieldError: String,
  val setUserEmailTextFieldError: StateSetter<String>,
  val userPasswordTextFieldValue: String,
  val setUserPasswordTextFieldValue: StateSetter<String>,
  val userPasswordTextFieldError: String,
  val setUserPasswordTextFieldError: StateSetter<String>,
  val userNameTextFieldValue: String,
  val setUserNameTextFieldValue: StateSetter<String>,
  val userNameTextFieldError: String,
  val setUserNameTextFieldError: StateSetter<String>,
  val userPermissions: Set<UserPermission>,
  val setUserPermissions: StateSetter<Set<UserPermission>>,
) {
  companion object {
    fun useUserController(
      user: ClientUser?,
      onFinished: (response: String?) -> Unit,
      launch: (suspend () -> Unit) -> Job
    ): UserController {
      var userCreationInProgress by useState(false)
      var (userEmailTextFieldValue, setUserEmailTextFieldValue) = useState(user?.email ?: "")
      var (userEmailTextFieldError, setUserEmailTextFieldError) = useState("")
      var (userPasswordTextFieldValue, setUserPasswordTextFieldValue) = useState("")
      var (userPasswordTextFieldError, setUserPasswordTextFieldError) = useState("")
      var (userNameTextFieldValue, setUserNameTextFieldValue) = useState(user?.name ?: "")
      var (userNameTextFieldError, setUserNameTextFieldError) = useState("")
      var (userPermissions, setUserPermissions) = useState(user?.permissions ?: setOf(UserPermission.EDIT_OWN_ACCESS))

      fun createNewUser() = launch {
        val appContext = useContext(appContextToInject)!!

        userCreationInProgress = true
        val response = NetworkManager.post<String>(
          url = "$apiBase/user/create",
          body = NewUserData(
            email = userEmailTextFieldValue,
            password = userPasswordTextFieldValue,
            name = userNameTextFieldValue,
            permissions = userPermissions.map { it.name }
          )
        )
        userCreationInProgress = false

        onFinished(response)
        val snackbarText = if (response == "ok") {
          Strings.user_created_account_details.get()
        } else {
          Strings.network_error.get()
        }
        appContext.showSnackbar(snackbarText)
      }

      fun editUser() = launch {
        val appContext = useContext(appContextToInject)!!

        userCreationInProgress = true
        val response = NetworkManager.post<String>(
          url = "$apiBase/user/edit",
          body = EditUserData(
            userId = user!!.id,
            name = userNameTextFieldValue.emptyToNull(),
            password = userPasswordTextFieldValue.emptyToNull(),
            permissions = userPermissions
              .takeIf { user.permissions != it }
              ?.map { it.name }
          )
        )
        val userData = appContext.userDataContext.userData!!
        if (response != null && user.id == userData.clientUser?.id) {
          // Only update currently logged-in user
          appContext.userDataContext.fetchNewUserData()
        }
        userCreationInProgress = false

        onFinished(response)
        val snackbarText = if (response == "ok") {
          Strings.user_updated_account_details.get()
        } else {
          Strings.network_error.get()
        }
        appContext.showSnackbar(snackbarText)
      }

      fun validateEmailInput(): Boolean {
        when {
          userEmailTextFieldValue.isEmpty() -> {
            userEmailTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.email_address.get())

            return false
          }

          userEmailTextFieldValue.count() < 6 -> {
            userEmailTextFieldError = Strings.parameter_too_short_format.get().format(Strings.email_address.get())

            return false
          }

          !userEmailTextFieldValue.matches(emailRegex) -> {
            userEmailTextFieldError = Strings.login_register_email_not_valid.get()
            return false
          }

          else -> return true
        }
      }

      fun validatePasswordInput(): Boolean {
        when {
          userPasswordTextFieldValue.count() < 6 -> {
            userPasswordTextFieldError = Strings.parameter_too_short_format.get().format(Strings.login_email_form_pw_label.get())

            return false
          }

          userPasswordTextFieldValue.isEmpty() -> {
            userPasswordTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.login_email_form_pw_label.get())

            return false
          }

          else -> return true
        }
      }

      fun validateNameInput(): Boolean {
        if (userNameTextFieldValue.isEmpty()) {
          userNameTextFieldError = Strings.parameter_cannot_be_empty_format.get().format(Strings.name.get())

          return false
        }
        return true
      }

      return UserController(
        createNewUser = ::createNewUser,
        editUser = ::editUser,
        validateNameInput = ::validateNameInput,
        validatePasswordInput = ::validatePasswordInput,
        validateEmailInput = ::validateEmailInput,
        userCreationInProgress = userCreationInProgress,
        userEmailTextFieldValue = userEmailTextFieldValue,
        setUserEmailTextFieldValue = setUserEmailTextFieldValue,
        userEmailTextFieldError = userEmailTextFieldError,
        setUserEmailTextFieldError = setUserEmailTextFieldError,
        userPasswordTextFieldValue = userPasswordTextFieldValue,
        setUserPasswordTextFieldValue = setUserPasswordTextFieldValue,
        userPasswordTextFieldError = userPasswordTextFieldError,
        setUserPasswordTextFieldError = setUserPasswordTextFieldError,
        userNameTextFieldValue = userNameTextFieldValue,
        setUserNameTextFieldValue = setUserNameTextFieldValue,
        userNameTextFieldError = userNameTextFieldError,
        setUserNameTextFieldError = setUserNameTextFieldError,
        userPermissions = userPermissions,
        setUserPermissions = setUserPermissions,
      )
    }
  }
}
