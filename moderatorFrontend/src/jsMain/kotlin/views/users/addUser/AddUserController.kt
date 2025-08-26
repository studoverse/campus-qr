package views.users.addUser

import app.appContextToInject
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.extensions.emailRegex
import com.studo.campusqr.common.extensions.emptyToNull
import com.studo.campusqr.common.extensions.format
import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.EditUserData
import com.studo.campusqr.common.payloads.NewUserData
import kotlinx.coroutines.Job
import react.dom.events.ChangeEvent
import react.useState
import util.Strings
import util.apiBase
import util.get
import web.html.HTMLInputElement
import webcore.Launch
import webcore.NetworkManager
import webcore.TextFieldOnChange

data class AddUserController(
  val userCreationInProgress: Boolean,
  val userEmailTextFieldValue: String,
  val userEmailTextFieldError: String,
  val userPasswordTextFieldValue: String,
  val userPasswordTextFieldError: String,
  val userNameTextFieldValue: String,
  val userNameTextFieldError: String,
  val userPermissions: Set<UserPermission>,

  val createNewUser: () -> Job,
  val editUser: () -> Job,
  val validateNameInput: () -> Boolean,
  val validatePasswordInput: () -> Boolean,
  val validateEmailInput: () -> Boolean,
  val userNameTextFieldOnChange: TextFieldOnChange,
  val userEmailTextFieldOnChange: TextFieldOnChange,
  val userPasswordTextFieldOnChange: TextFieldOnChange,
  val userPermissionsOnChange: (userPermission: UserPermission, event: ChangeEvent<HTMLInputElement>, checked: Boolean) -> Unit,
) {
  companion object {
    fun use(
      user: ClientUser?,
      onFinished: (response: String?) -> Unit,
      launch: Launch
    ): AddUserController {
      var userCreationInProgress by useState(false)
      var userEmailTextFieldValue by useState(user?.email ?: "")
      var userEmailTextFieldError by useState("")
      var userPasswordTextFieldValue by useState("")
      var userPasswordTextFieldError by useState("")
      var userNameTextFieldValue by useState(user?.name ?: "")
      var userNameTextFieldError by useState("")
      var userPermissions by useState(user?.permissions ?: setOf(UserPermission.EDIT_OWN_ACCESS))

      fun createNewUser() = launch {
        val appContext = react.use(appContextToInject)!!

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
        appContext.showSnackbarText(snackbarText)
      }

      fun editUser() = launch {
        val appContext = react.use(appContextToInject)!!

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
        appContext.showSnackbarText(snackbarText)
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

      val userNameTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        userNameTextFieldValue = value
        userNameTextFieldError = ""
      }

      val userEmailTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        userEmailTextFieldValue = value
        userEmailTextFieldError = ""
      }

      val userPasswordTextFieldOnChange: TextFieldOnChange = { event ->
        val value = event.target.value
        userPasswordTextFieldValue = value
        userPasswordTextFieldError = ""
      }

      fun userPermissionsOnChange(userPermission: UserPermission, event: ChangeEvent<HTMLInputElement>, checked: Boolean) {
        val existingPermissions = userPermissions
        userPermissions = if (checked) {
          existingPermissions + userPermission
        } else {
          existingPermissions - userPermission
        }
      }

      return AddUserController(
        userCreationInProgress = userCreationInProgress,
        userEmailTextFieldValue = userEmailTextFieldValue,
        userEmailTextFieldError = userEmailTextFieldError,
        userPasswordTextFieldValue = userPasswordTextFieldValue,
        userPasswordTextFieldError = userPasswordTextFieldError,
        userNameTextFieldValue = userNameTextFieldValue,
        userNameTextFieldError = userNameTextFieldError,
        userPermissions = userPermissions,
        createNewUser = ::createNewUser,
        editUser = ::editUser,
        validateNameInput = ::validateNameInput,
        validatePasswordInput = ::validatePasswordInput,
        validateEmailInput = ::validateEmailInput,
        userNameTextFieldOnChange = userNameTextFieldOnChange,
        userEmailTextFieldOnChange = userEmailTextFieldOnChange,
        userPasswordTextFieldOnChange = userPasswordTextFieldOnChange,
        userPermissionsOnChange = ::userPermissionsOnChange,
      )
    }
  }
}