package views.login

import app.GlobalCss
import csstype.ClassName
import mui.material.*
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.Strings
import util.get
import web.history.history
import web.html.ButtonType
import webcore.RComponent

class LoginNavigationButtonsViewConfig(
  var networkRequestInProgress: Boolean,
  var backEnabled: Boolean,
  var nextButtonText: String,
  var nextButtonDisabled: Boolean,
  var onNextAction: () -> Unit
)

external interface LoginNavigationButtonsViewProps : Props {
  var config: LoginNavigationButtonsViewConfig
}

external interface LoginNavigationButtonsViewState : State

private class LoginNavigationButtonsView : RComponent<LoginNavigationButtonsViewProps, LoginNavigationButtonsViewState>() {
  override fun ChildrenBuilder.render() {
    Box {
      className = ClassName(GlobalCss.flex)
      if (props.config.backEnabled) {
        Button {
          variant = ButtonVariant.outlined
          color = ButtonColor.primary
          onClick = {
            history.back()
          }
          +Strings.back.get().uppercase()
        }
      }
      Box {
        className = ClassName(GlobalCss.flexEnd)
        if (props.config.networkRequestInProgress) {
          CircularProgress()
        } else {
          Button {
            variant = ButtonVariant.outlined
            color = ButtonColor.primary
            disabled = props.config.nextButtonDisabled
            onClick = {
              props.config.onNextAction()
            }
            type = ButtonType.submit
            +props.config.nextButtonText
          }
        }
      }
    }
  }
}

fun ChildrenBuilder.renderLoginNavigationButtonsView(config: LoginNavigationButtonsViewConfig) {
  LoginNavigationButtonsView::class.react {
    this.config = config
  }
}
