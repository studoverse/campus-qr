package views.users.addUser

import com.studo.campusqr.common.payloads.ClientUser

sealed class AddUserConfig(val onFinished: (response: String?) -> Unit) {
  class Create(onFinished: (response: String?) -> Unit) : AddUserConfig(onFinished)
  class Edit(val user: ClientUser, onFinished: (response: String?) -> Unit) : AddUserConfig(onFinished)
}