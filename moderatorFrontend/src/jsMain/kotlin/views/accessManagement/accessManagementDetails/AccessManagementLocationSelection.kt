package views.accessManagement.accessManagementDetails

import com.studo.campusqr.common.payloads.ClientLocation
import js.lazy.Lazy
import js.objects.Object
import mui.material.Autocomplete
import mui.material.AutocompleteProps
import mui.material.FormControlVariant
import mui.material.TextField
import react.Fragment
import react.Props
import react.create
import util.Strings
import util.get
import webcore.AutocompleteOnChange
import webcore.FcWithCoroutineScope
import webcore.toReactNode

data class AccessManagementLocationSelectionConfig(
  val isAutocompleteDisabled: Boolean,
  val selectedLocation: ClientLocation?,
  val selectedLocationTextFieldError: String,
  val locationNameToLocationMap: Map<String, ClientLocation>,
  val locationSelectionOnChange: AutocompleteOnChange<String>,
)

external interface AccessManagementLocationSelectionProps : Props {
  var config: AccessManagementLocationSelectionConfig
}

//@Lazy
val AccessManagementLocationSelectionFc = FcWithCoroutineScope<AccessManagementLocationSelectionProps> { props, launch ->

  Autocomplete<AutocompleteProps<String>> {
    disabled = props.config.isAutocompleteDisabled
    value = props.config.selectedLocation?.name
    onChange = props.config.locationSelectionOnChange
    openOnFocus = true
    options = props.config.locationNameToLocationMap.keys.toTypedArray()
    getOptionLabel = { it }
    renderInput = { params ->
      Fragment.create {
        TextField {
          Object.assign(this, params)
          error = props.config.selectedLocationTextFieldError.isNotEmpty()
          helperText = props.config.selectedLocationTextFieldError.toReactNode()
          fullWidth = true
          variant = FormControlVariant.outlined
          label = Strings.location_name.get().toReactNode()
        }
      }
    }
  }

}