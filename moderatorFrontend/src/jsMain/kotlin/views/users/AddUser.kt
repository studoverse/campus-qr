package views.users

import web.cssom.*
import app.GlobalCss
import app.appContextToInject
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.payloads.ClientUser
import com.studo.campusqr.common.payloads.canEditUsers
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import util.Strings
import util.get
import util.localizedString
import views.common.MbLinearProgressFc
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

val AddUserFc = FcWithCoroutineScope<AddUserProps> { props, launch ->
  var addUserController = AddUserController.useUserController(
    user = (props.config as? AddUserConfig.Edit)?.user,
    onFinished = props.config.onFinished,
    launch = launch,
  )

  MbLinearProgressFc { show = addUserController.userCreationInProgress }

  val appContext = useContext(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!
  TextField {
    key = "userEmailTextField"
    error = addUserController.userEmailTextFieldError.isNotEmpty()
    helperText = addUserController.userEmailTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    value = addUserController.userEmailTextFieldValue
    autoComplete = "username"
    label = Strings.email_address.get().toReactNode()
    type = InputType.email
    if (props.config is AddUserConfig.Edit) {
      disabled = true
    }
    onChange = addUserController.userEmailTextFieldOnChange
  }

  spacer(16, key = "userEmailSpacer")

  if (!userData.externalAuthProvider) {
    TextField {
      key = "userPasswordTextField"
      error = addUserController.userPasswordTextFieldError.isNotEmpty()
      helperText = addUserController.userPasswordTextFieldError.toReactNode()
      fullWidth = true
      type = InputType.password
      variant = FormControlVariant.outlined
      if (props.config is AddUserConfig.Create) {
        label = Strings.login_email_form_pw_label.get().toReactNode()
      } else {
        label = Strings.login_email_form_new_pw_label.get().toReactNode()
        autoComplete = "new-password"
      }
      value = addUserController.userPasswordTextFieldValue
      onChange = addUserController.userPasswordTextFieldOnChange
    }
    spacer(16, key = "userPasswordSpacer")
  }

  TextField {
    key = "userNameTextField"
    error = addUserController.userNameTextFieldError.isNotEmpty()
    helperText = addUserController.userNameTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    label = Strings.user_name.get().toReactNode()
    value = addUserController.userNameTextFieldValue
    autoComplete = "off"
    onChange = addUserController.userNameTextFieldOnChange
  }

  spacer(16, key = "userNameSpacer")

  // This view is either used for user management, or to change own user properties
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
            key = userPermission.name
            control = Checkbox.create {
              checked = userPermission in addUserController.userPermissions
              onChange = { event, checked -> addUserController.userPermissionsOnChange(userPermission, event, checked) }
            }
            label = userPermission.localizedString.get().toReactNode()
          }
        }
      }
    }
  }

  spacer(32, key = "userPermissionsSpacer")

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
              if (addUserController.validateNameInput() && addUserController.validatePasswordInput() && addUserController.validateEmailInput()) {
                addUserController.createNewUser()
              }
            }

            is AddUserConfig.Edit -> {
              addUserController.editUser()
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
