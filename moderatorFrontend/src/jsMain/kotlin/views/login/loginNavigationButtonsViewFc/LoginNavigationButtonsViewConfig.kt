package views.login.loginNavigationButtonsViewFc

class LoginNavigationButtonsViewConfig(
  var networkRequestInProgress: Boolean,
  var backEnabled: Boolean,
  var nextButtonText: String,
  var nextButtonDisabled: Boolean,
  var onNextAction: () -> Unit
)
