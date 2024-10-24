package views.report

import app.GlobalCss.flex
import com.studo.campusqr.common.payloads.ReportData
import web.cssom.*
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.spacer
import webcore.FcWithCoroutineScope
import webcore.MbDialogRef
import webcore.toReactNode

class AddFilterConfig(
  val userLocation: ReportData.UserLocation,
  val dialogRef: MutableRefObject<MbDialogRef>,
  val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
)

external interface AddFilterProps : Props {
  var config: AddFilterConfig
}

val AddFilter = FcWithCoroutineScope<AddFilterProps> { props, launch ->
  val addFilterController = AddFilterController.useAddFilterController(
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
      onChange = addFilterController.autocompleteOnChange
      onInputChange = addFilterController.autocompleteOnInputChange
      disableCloseOnSelect = true
      fullWidth = true
      multiple = true
      openOnFocus = true
      options = addFilterController.filterOptions.map { it.toString() }.toTypedArray()
      value = addFilterController.filteredSeats.map { it.toString() }.toTypedArray()
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
      onClick = addFilterController.applyButtonOnClick
    }
  }
}
