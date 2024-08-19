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
import webcore.MbDialog
import webcore.RComponentWithCoroutineScope
import webcore.setState
import webcore.toReactNode

class AddFilterDialogConfig(
  val userLocation: ReportData.UserLocation,
  val dialogRef: RefObject<MbDialog>,
  val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
)

external interface AddFilterDialogProps : Props {
  var config: AddFilterDialogConfig
}

external interface AddFilterDialogState : State {
  var filterOptions: List<Int>
  var filteredSeats: List<Int>
}

class AddFilterDialog(props: AddFilterDialogProps) :
  RComponentWithCoroutineScope<AddFilterDialogProps, AddFilterDialogState>(props) {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(AddFilterDialog::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun AddFilterDialogState.init(props: AddFilterDialogProps) {
    filterOptions = props.config.userLocation.locationSeatCount?.let { seatCount ->
      (1..seatCount).map { it }.filter { it != props.config.userLocation.seat }
    } ?: emptyList()
    filteredSeats = props.config.userLocation.filteredSeats?.toList() ?: emptyList()
  }

  override fun ChildrenBuilder.render() {
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
        onChange = { _, target: Any, _, _ ->
          @Suppress("UNCHECKED_CAST")
          target as Array<String>
          setState {
            filteredSeats = target.map { it.toInt() }
          }
        }
        onInputChange = { _, value, _ ->
          // Add values after user pressed a " " or "," for fast input
          if (value.endsWith(" ") || value.endsWith(",")) {
            val seatNumber = value.trim().removeSuffix(",").toIntOrNull()
            if (seatNumber != null && seatNumber in state.filterOptions && seatNumber !in state.filteredSeats) {
              setState {
                filteredSeats = filteredSeats + seatNumber
              }
            }
          }
        }
        disableCloseOnSelect = true
        fullWidth = true
        multiple = true
        openOnFocus = true
        options = state.filterOptions.map { it.toString() }.toTypedArray()
        value = state.filteredSeats.map { it.toString() }.toTypedArray()
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
            onApplyFilterChange(userLocation, state.filteredSeats)
          }
          props.config.dialogRef.current!!.closeDialog()
        }
      }
    }
  }
}