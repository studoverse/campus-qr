package views.report

import app.AppContext
import app.GlobalCss.flex
import app.appContextToInject
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
  var filterOptions: List<Int> = props.config.userLocation.locationSeatCount?.let { seatCount ->
    (1..seatCount).map { it }.filter { it != props.config.userLocation.seat }
  } ?: emptyList()
  var filteredSeats: List<Int> = props.config.userLocation.filteredSeats?.toList() ?: emptyList()

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
      // TODO: @mb
      onChange = { _, target: Any, _, _ ->
        @Suppress("UNCHECKED_CAST")
        target as Array<String>
        filteredSeats = target.map { it.toInt() }
      }
      onInputChange = { _, value, _ ->
        // Add values after user pressed a " " or "," for fast input
        if (value.endsWith(" ") || value.endsWith(",")) {
          val seatNumber = value.trim().removeSuffix(",").toIntOrNull()
          if (seatNumber != null && seatNumber in filterOptions && seatNumber !in filteredSeats) {
            filteredSeats = filteredSeats + seatNumber
          }
        }
      }
      disableCloseOnSelect = true
      fullWidth = true
      multiple = true
      openOnFocus = true
      options = filterOptions.map { it.toString() }.toTypedArray()
      value = filteredSeats.map { it.toString() }.toTypedArray()
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
      onClick = {
        with(props.config) {
          onApplyFilterChange(userLocation, filteredSeats)
        }
        props.config.dialogRef.current!!.closeDialog()
      }
    }
  }
}
