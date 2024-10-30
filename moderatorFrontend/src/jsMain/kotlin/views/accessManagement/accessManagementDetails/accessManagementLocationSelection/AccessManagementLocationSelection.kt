package views.accessManagement.accessManagementDetails.accessManagementLocationSelection

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
import webcore.FcWithCoroutineScope
import webcore.toReactNode

external interface AccessManagementLocationSelectionProps : Props {
  var config: AccessManagementLocationSelectionConfig
}

@Lazy
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