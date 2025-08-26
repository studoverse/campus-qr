package views.login.loginNavigationButtonsViewFc

import app.GlobalCss
import js.lazy.Lazy
import web.cssom.*
import mui.material.*
import react.Props
import util.Strings
import util.get
import web.history.history
import web.html.ButtonType
import web.html.submit
import webcore.FcWithCoroutineScope

external interface LoginNavigationButtonsViewProps : Props {
  var config: LoginNavigationButtonsViewConfig
}

@Lazy
val LoginNavigationButtonsView = FcWithCoroutineScope<LoginNavigationButtonsViewProps> { props, componentScope ->
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