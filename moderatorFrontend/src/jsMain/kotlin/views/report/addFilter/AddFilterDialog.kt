package views.report.addFilter

import js.lazy.Lazy
import app.GlobalCss.flex
import web.cssom.*
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.spacer
import webcore.FcWithCoroutineScope
import webcore.toReactNode

external interface AddFilterProps : Props {
  var config: AddFilterConfig
}

@Lazy
val AddFilter = FcWithCoroutineScope<AddFilterProps> { props, launch ->
  val controller = AddFilterController.useAddFilterController(
    launch = launch,
    config = props.config,
  )

  Typography {
    +Strings.report_checkin_add_filter_content.get()
  }

  spacer(16)

  Box {
    sx {
      // Make sure that dialog's apply button doesn't get overlaid by autocomplete's dropdown
      width = 100.pct - 70.px
    }
    Autocomplete<AutocompleteProps<String>> {
      onChange = controller.autocompleteOnChange
      onInputChange = controller.autocompleteOnInputChange
      disableCloseOnSelect = true
      fullWidth = true
      multiple = true
      openOnFocus = true
      options = controller.filterOptions.map { it.toString() }.toTypedArray()
      value = controller.filteredSeats.map { it.toString() }.toTypedArray()
      getOptionLabel = { it }
      renderInput = { params ->
        TextField.create {
          +params
          variant = FormControlVariant.outlined
          label = Strings.report_checkin_seat_filter.get().toReactNode()
        }
      }
    }
  }
  Box {
    sx {
      flex()
    }
    Button {
      sx {
        marginLeft = Auto.auto
      }
      +Strings.apply.get()
      onClick = controller.applyButtonOnClick
    }
  }
}
