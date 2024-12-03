package views.accessManagement.accessManagementDetails.permittedPeople

import webcore.ButtonOnClick
import webcore.Launch

data class PermittedPeopleController(
  val addPersonButtonOnClick: ButtonOnClick,
  val removePermittedPeopleButtonOnClick: (personIdentification: String) -> Unit,
) {
  companion object {
    fun use(config: PermittedPeopleConfig, launch: Launch): PermittedPeopleController {
      val addPersonButtonOnClick: ButtonOnClick = {
        config.submitPermittedPeopleToState()
      }

      fun removePermittedPeopleButtonOnClick(
        personIdentification: String,
      ) {
        config.removePermittedPeopleOnClick(personIdentification)
      }

      return PermittedPeopleController(
        addPersonButtonOnClick = addPersonButtonOnClick,
        removePermittedPeopleButtonOnClick = ::removePermittedPeopleButtonOnClick,
      )
    }
  }
}