package views.users.listUsers

import app.appContextToInject
import com.studo.campusqr.common.payloads.ClientUser
import react.useEffectOnce
import react.useState
import util.Strings
import util.apiBase
import util.get
import webcore.Launch
import webcore.NetworkManager

data class ListUsersController(
  val userList: List<ClientUser>?,
  val loadingUserList: Boolean,
  val handleCreateOrAddUserResponse: (response: String?) -> Unit,
) {
  companion object {
    fun use(
      launch: Launch
    ): ListUsersController {
      var userList by useState<List<ClientUser>?>(null)
      var loadingUserList by useState(false)

      val appContext = react.use(appContextToInject)!!

      fun fetchUserList() = launch {
        loadingUserList = true
        val response = NetworkManager.get<Array<ClientUser>>("$apiBase/user/list")
        userList = response?.toList()
        loadingUserList = false
      }

      useEffectOnce {
        fetchUserList()
      }

      fun handleCreateOrAddUserResponse(response: String?) {
        val snackbarText = when (response) {
          "already_exists" -> Strings.user_already_exists.get()
          "ok" -> {
            fetchUserList()
            Strings.user_created.get()
          }

          else -> Strings.error_try_again.get()
        }
        appContext.showSnackbarText(snackbarText)
      }

      return ListUsersController(
        userList = userList,
        loadingUserList = loadingUserList,
        handleCreateOrAddUserResponse = ::handleCreateOrAddUserResponse,
      )
    }
  }
}