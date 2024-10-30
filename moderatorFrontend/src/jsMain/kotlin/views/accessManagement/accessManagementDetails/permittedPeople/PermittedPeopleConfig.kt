package views.accessManagement.accessManagementDetails.permittedPeople

import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import webcore.TextFieldOnChange

data class PermittedPeopleConfig(
  val permittedPeopleList: List<String>,
  val personEmailTextFieldValue: String,
  val accessManagementDetailsType: AccessManagementDetailsConfig,
  val submitPermittedPeopleToState: () -> Unit,
  val addPermittedPeopleOnChange: TextFieldOnChange,
  val removePermittedPeopleOnClick: (personIdentification: String) -> Unit,
)