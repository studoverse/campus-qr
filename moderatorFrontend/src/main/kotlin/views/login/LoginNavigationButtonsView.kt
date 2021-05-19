package views.login

import app.GlobalCss
import kotlinx.browser.window
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.div
import util.Strings
import util.get
import webcore.materialUI.circularProgress
import webcore.materialUI.muiButton
import webcore.materialUI.withStyles

interface LoginNavigationButtonsViewProps : RProps {
  class Config(
    var networkRequestInProgress: Boolean,
    var backEnabled: Boolean,
    var nextButtonText: String,
    var nextButtonDisabled: Boolean,
    var onNextAction: () -> Unit
  )

  var config: Config
  var classes: LoginNavigationButtonsViewClasses
}

interface LoginNavigationButtonsViewState : RState

class LoginNavigationButtonsView : RComponent<LoginNavigationButtonsViewProps, LoginNavigationButtonsViewState>() {
  override fun RBuilder.render() {
    div(GlobalCss.flex) {
      if (props.config.backEnabled) {
        muiButton {
          attrs.variant = "outlined"
          attrs.color = "primary"
          attrs.onClick = {
            window.history.back()
          }
          +Strings.back.get().toUpperCase()
        }
      }
      div(GlobalCss.flexEnd) {
        if (props.config.networkRequestInProgress) {
          circularProgress {}
        } else {
          muiButton {
            attrs.variant = "outlined"
            attrs.color = "primary"
            attrs.disabled = props.config.nextButtonDisabled
            attrs.onClick = {
              props.config.onNextAction()
            }
            attrs.type = "submit"
            +props.config.nextButtonText
          }
        }
      }
    }
  }
}

interface LoginNavigationButtonsViewClasses

private val style = { _: dynamic ->
}

private val styled =
  withStyles<LoginNavigationButtonsViewProps, LoginNavigationButtonsView>(style)

fun RBuilder.renderLoginNavigationButtonsView(config: LoginNavigationButtonsViewProps.Config) = styled {
  attrs.config = config
}
  