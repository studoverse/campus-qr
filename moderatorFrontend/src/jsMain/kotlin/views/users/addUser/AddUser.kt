package views.users.addUser

import web.cssom.*
import app.GlobalCss
import app.appContextToInject
import com.studo.campusqr.common.UserPermission
import com.studo.campusqr.common.payloads.canEditUsers
import js.lazy.Lazy
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import util.Strings
import util.get
import util.localizedString
import views.common.MbLinearProgress
import views.common.spacer
import web.html.InputType
import web.html.email
import web.html.password
import webcore.*

external interface AddUserProps : Props {
  var config: AddUserConfig
}

@Lazy
val AddUser = FcWithCoroutineScope<AddUserProps> { props, launch ->
  var controller = AddUserController.use(
    user = (props.config as? AddUserConfig.Edit)?.user,
    onFinished = props.config.onFinished,
    launch = launch,
  )

  MbLinearProgress { show = controller.userCreationInProgress }

  val appContext = use(appContextToInject)!!
  val userData = appContext.userDataContext.userData!!
  TextField {
    key = "userEmailTextField"
    error = controller.userEmailTextFieldError.isNotEmpty()
    helperText = controller.userEmailTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    value = controller.userEmailTextFieldValue
    autoComplete = "username"
    label = Strings.email_address.get().toReactNode()
    type = InputType.email
    if (props.config is AddUserConfig.Edit) {
      disabled = true
    }
    onChange = controller.userEmailTextFieldOnChange
  }

  spacer(16, key = "userEmailSpacer")

  if (!userData.externalAuthProvider) {
    TextField {
      key = "userPasswordTextField"
      error = controller.userPasswordTextFieldError.isNotEmpty()
      helperText = controller.userPasswordTextFieldError.toReactNode()
      fullWidth = true
      type = InputType.password
      variant = FormControlVariant.outlined
      if (props.config is AddUserConfig.Create) {
        label = Strings.login_email_form_pw_label.get().toReactNode()
      } else {
        label = Strings.login_email_form_new_pw_label.get().toReactNode()
        autoComplete = "new-password"
      }
      value = controller.userPasswordTextFieldValue
      onChange = controller.userPasswordTextFieldOnChange
    }
    spacer(16, key = "userPasswordSpacer")
  }

  TextField {
    key = "userNameTextField"
    error = controller.userNameTextFieldError.isNotEmpty()
    helperText = controller.userNameTextFieldError.toReactNode()
    fullWidth = true
    variant = FormControlVariant.outlined
    label = Strings.user_name.get().toReactNode()
    value = controller.userNameTextFieldValue
    autoComplete = "off"
    onChange = controller.userNameTextFieldOnChange
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
              checked = userPermission in controller.userPermissions
              onChange = { event, checked -> controller.userPermissionsOnChange(userPermission, event, checked) }
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
              if (controller.validateNameInput() && controller.validatePasswordInput() && controller.validateEmailInput()) {
                controller.createNewUser()
              }
            }

            is AddUserConfig.Edit -> {
              controller.editUser()
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
