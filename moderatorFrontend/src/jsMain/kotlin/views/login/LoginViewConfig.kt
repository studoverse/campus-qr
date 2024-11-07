package views.login

class LoginViewConfig(
  val loginMode: LoginMode,
) {
  companion object {
    enum class LoginMode {
      EMAIL
    }
  }
}
