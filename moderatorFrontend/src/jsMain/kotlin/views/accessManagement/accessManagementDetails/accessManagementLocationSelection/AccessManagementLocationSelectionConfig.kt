package views.accessManagement.accessManagementDetails.accessManagementLocationSelection

import com.studo.campusqr.common.payloads.ClientLocation
import webcore.AutocompleteOnChange

data class AccessManagementLocationSelectionConfig(
  val isAutocompleteDisabled: Boolean,
  val selectedLocation: ClientLocation?,
  val selectedLocationTextFieldError: String,
  val locationNameToLocationMap: Map<String, ClientLocation>,
  val locationSelectionOnChange: AutocompleteOnChange<String>,
)