package views.accessManagement.accessManagementDetails.permittedPeople

import app.GlobalCss
import js.lazy.Lazy
import mui.icons.material.Close
import mui.material.Box
import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.FormControlVariant
import mui.material.IconButton
import mui.material.Size
import mui.material.Table
import mui.material.TableBody
import mui.material.TableCell
import mui.material.TableCellAlign
import mui.material.TableHead
import mui.material.TableRow
import mui.material.TextField
import mui.material.Typography
import mui.system.sx
import react.Props
import react.dom.html.ReactHTML.form
import util.Strings
import util.get
import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import views.common.spacer
import web.cssom.ClassName
import web.cssom.pct
import webcore.FcWithCoroutineScope
import webcore.onChange
import webcore.toReactNode

external interface PermittedPeopleProps : Props {
  var config: PermittedPeopleConfig
}

@Lazy
val PermittedPeople = FcWithCoroutineScope<PermittedPeopleProps> { props, launch ->
  val controller = PermittedPeopleController.usePermittedPeopleController(
    config = props.config,
    launch = launch,
  )

  Typography {
    +Strings.access_control_permitted_people.get()
  }
  spacer(12, key = "permittedPeopleSpacer1")
  Box {
    component = form
    sx {
      width = 100.pct
    }
    onSubmit = { event ->
      if (props.config.accessManagementDetailsType !is AccessManagementDetailsConfig.Details) {
        props.config.submitPermittedPeopleToState()
      }
      event.preventDefault()
      event.stopPropagation()
    }
    if (props.config.accessManagementDetailsType !is AccessManagementDetailsConfig.Details) {
      Box {
        className = ClassName(GlobalCss.flex)
        TextField {
          disabled = props.config.accessManagementDetailsType is AccessManagementDetailsConfig.Details
          helperText = Strings.access_control_add_permitted_people_tip.get().toReactNode()
          fullWidth = true
          variant = FormControlVariant.outlined
          label = Strings.email_address.get().toReactNode()
          value = props.config.personEmailTextFieldValue
          onChange = props.config.addPermittedPeopleOnChange
        }

        Box {
          className = ClassName(GlobalCss.flexEnd)
          spacer(key = "addPersonSpacer1")
          Button {
            size = Size.small
            color = ButtonColor.primary
            variant = ButtonVariant.outlined
            onClick = controller.addPersonButtonOnClick
            +Strings.access_control_add_permitted_people.get()
          }
        }
      }
    }
  }

  if (props.config.permittedPeopleList.isNotEmpty()) {
    Table {
      TableHead {
        TableRow {
          TableCell { +Strings.email_address.get() }
          TableCell { }
        }
      }
      TableBody {
        props.config.permittedPeopleList.forEach { personIdentification ->
          TableRow {
            TableCell {
              +personIdentification
            }

            TableCell {
              align = TableCellAlign.right
              if (props.config.accessManagementDetailsType !is AccessManagementDetailsConfig.Details) {
                IconButton {
                  Close()
                  onClick = {
                    controller.removePermittedPeopleButtonOnClick(personIdentification)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}