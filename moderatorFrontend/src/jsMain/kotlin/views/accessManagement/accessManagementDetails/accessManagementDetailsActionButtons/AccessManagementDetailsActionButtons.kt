package views.accessManagement.accessManagementDetails.accessManagementDetailsActionButtons

import app.GlobalCss
import js.lazy.Lazy
import mui.material.Box
import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.system.sx
import react.Props
import util.Strings
import util.get
import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import web.cssom.ClassName
import web.cssom.px
import webcore.FcWithCoroutineScope

external interface AccessManagementDetailsActionButtonsProps : Props {
  var config: AccessManagementDetailsActionButtonsConfig
}

@Lazy
val AccessManagementDetailsActionButtons =
  FcWithCoroutineScope<AccessManagementDetailsActionButtonsProps> { props, launch ->
    val createButtonText = when (props.config.accessManagementDetailsType) {
      is AccessManagementDetailsConfig.Create -> Strings.access_control_create.get()
      is AccessManagementDetailsConfig.Edit -> Strings.access_control_save.get()
      is AccessManagementDetailsConfig.Details -> ""
    }
    if (createButtonText.isNotEmpty()) {
      Box {
        className = ClassName(GlobalCss.flex)
        Box {
          sx {
            marginBottom = 16.px
          }
          className = ClassName(GlobalCss.flexEnd)
          Button {
            sx {
              marginRight = 16.px
            }
            +Strings.cancel.get()
            variant = ButtonVariant.text
            onClick = {

              props.config.dialogRef.current!!.closeDialog()
            }
          }
          Button {
            variant = ButtonVariant.contained
            color = ButtonColor.primary
            onClick = {
              props.config.createAccessControlOnClick()
            }
            +createButtonText
          }
        }
      }
    }
  }